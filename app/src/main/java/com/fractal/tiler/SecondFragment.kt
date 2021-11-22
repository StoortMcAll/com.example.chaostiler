package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.MainActivity.Companion.ImageFilter
import com.fractal.tiler.MainActivity.Companion.mEnableDataClone
import com.fractal.tiler.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.String as KotlinString

// endregion


class SecondFragment : Fragment() {
    private lateinit var seekbar: MySeekbar
    lateinit var isLinearView : Button
    lateinit var imageButton: ImageButton

    val mThisPageID = 1

    var jobTextures : Job? = null

    var calcActive = false

    private var _fragmentSecondBinding : FragmentSecondBinding? = null
    private val binding get() = _fragmentSecondBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        _fragmentSecondBinding = FragmentSecondBinding.inflate(inflater, container, false)

        if (mEnableDataClone) {
            pixelDataClone = pixelData.clone()
        }

        binding.paletteScaler?.setImageBitmap(MainActivity.colorClass.aCurrentRange.colorRangeBitmap)

        MainActivity.colorClass.mNewColors = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        MainActivity.mCurrentPageID = mThisPageID

       /* if (mEnableDataClone) {
            pixelDataClone = pixelData.clone()
        }*/

        seekbar = view.findViewById(R.id.seekBar)
      /*  seekbar.progress = bitmapColorSpread.aCurrentRange.progressIncrement
        seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond
*/
        imageButton = view.findViewById(R.id.palette_scaler)

        isLinearView = view.findViewById(R.id.data_to_colour_type)

        setAnalysisButtonTitle()

        val tileimageview : MyImageView = view.findViewById(R.id.tile_image_view)
        //tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        tileimageview.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        setTileImageView(tileimageview)// Set reference to MyImageView in RunGenTasks

        //updateTextures(false)
        setTileViewBitmap(pixelDataClone)

        seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {


                var isStarted = false

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (!isStarted) {
                        if (fromUser) {
                            isStarted = true
                        }
                    } else {
                        seekBar.progress = progress
                        if (jobTextures == null || jobTextures?.isActive == false) {
                            MainActivity.colorClass.setProgress(seekBar.progress)

                            if (!calcActive) {
                                updateTextures()
                            }
                        }

                        return
                    }

                    if (isStarted){
                        seekBar.progress = progress
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (jobTextures!= null && jobTextures?.isActive == true){
                        jobTextures?.cancel()
                    }

                    seekBar.progress.also { seekBar.secondaryProgress = it }
                    MainActivity.colorClass.setProgress(seekBar.progress)
                    MainActivity.colorClass.aCurrentRange.progressSecond = seekBar.progress

                    if (!calcActive) {
                        updateTextures()
                    }

                    isStarted = false
                }

            }
        )

        view.findViewById<Button>(R.id.undo_all_changes).setOnClickListener {
            MainActivity.scopeIO.launch {
                if (!calcActive) {
                    calcActive = true

                    pixelDataClone = pixelData.clone()

                    updateTextures()

                    calcActive = false
                }
            }
        }

        view.findViewById<Button>(R.id.set_to_zero).setOnClickListener {
            MainActivity.scopeIO.launch {
                if (!calcActive) {
                    calcActive = true

                    runSetToZero()

                    updateTextures()

                    calcActive = false
                }
            }
        }

        view.findViewById<Button>(R.id.data_to_colour_type).setOnClickListener {
            switchProcessType()

            seekbar.progress = MainActivity.colorClass.getProgress()
            seekbar.secondaryProgress = seekbar.progress

            setAnalysisButtonTitle()

            if (!calcActive) {
                updateTextures()
            }
        }

        val blurText = view.findViewById<TextView>(R.id.blur)
        blurText.text = filter.toString()

        view.findViewById<Button>(R.id.blur_left).setOnClickListener {
            MainActivity.scopeIO.launch {
                if (!calcActive) {
                    calcActive = true

                    filter = when (filter) {
                        ImageFilter.Blur -> {
                            ImageFilter.Gaussian
                        }
                        ImageFilter.Gaussian -> {
                            ImageFilter.Motion
                        }
                        ImageFilter.Motion -> {
                            ImageFilter.BoxBlur
                        }
                        ImageFilter.BoxBlur -> {
                            ImageFilter.Median
                        }
                        ImageFilter.Median -> {
                            ImageFilter.Blur
                        }
                    }
                    //blurLeft()

                    //updateTextures()
                    CoroutineScope(Dispatchers.Main).launch {
                        blurText.text = filter.toString()
                    }

                    calcActive = false
                }
            }
        }

        view.findViewById<Button>(R.id.blur_right).setOnClickListener {
            MainActivity.scopeIO.launch {
                if (!calcActive) {
                    calcActive = true

                    when (filter) {
                        ImageFilter.BoxBlur -> {
                            BoxBlur.doImageFilter(pixelDataClone)
                        }
                        ImageFilter.Blur -> {
                            Blur.doImageFilter(pixelDataClone)
                        }
                        ImageFilter.Gaussian -> {
                            Gaussian.doImageFilter(pixelDataClone)
                        }
                        ImageFilter.Motion -> {
                            Motion.doImageFilter(pixelDataClone)
                        }
                        ImageFilter.Median -> {
                            Median.doImageFilter(pixelDataClone)
                        }
                    }
                    //blurRight()

                    updateTextures()

                    calcActive = false
                }
            }
        }


        view.findViewById<Button>(R.id.palette_left).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.selectPrevColorRange()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.selectNextColorRange()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }


        view.findViewById<Button>(R.id.add_new_palette).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.addNewColorA()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.add_new_palette2).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.addNewColorB()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.to_savewallpaper).setOnClickListener {

            //(this.activity as AppCompatActivity).supportActionBar?.hide()

            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }
    }

    private fun updateTextures(setTileView: Boolean = true) {
        jobTextures = CoroutineScope(Dispatchers.IO).launch {


            CoroutineScope(Dispatchers.Main).launch {
                if (MainActivity.colorClass.mNewColors) {
                    imageButton.setImageBitmap(MainActivity.colorClass.aCurrentRange.colorRangeBitmap)
                    MainActivity.colorClass.mNewColors = false
                }
            }

            if (setTileView) setTileViewBitmap(pixelDataClone)
        }
    }

    private fun setAnalysisButtonTitle(){
        val text : KotlinString = if (MainActivity.colorClass.aCurrentRange.dataProcess == DataProcess.LINEAR) {
            "Linear"
        } else {
            "Statistical"
        }
        isLinearView.text = text.subSequence(0, text.length)
    }

}