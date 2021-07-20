package com.example.chaostiler

// region Variable Declaration

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.FirstFragment.Companion.tileImageView
import com.example.chaostiler.MainActivity.Companion.DataProcess
import com.example.chaostiler.MainActivity.Companion.bitmapColorSpread
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone
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

    var mustCalcTile = false
    val mThisPageID = 1

    var jobTextures : Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()
        super.onViewCreated(view, savedInstanceState)

        if (MainActivity.mCurrentPageID > mThisPageID) {
            if (activity?.supportFragmentManager?.backStackEntryCount!! > 1) {
                activity?.supportFragmentManager?.popBackStack()
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        MainActivity.mCurrentPageID = mThisPageID

        if (mEnableDataClone) {
            pixelDataClone = pixelData.Clone()
        }

        seekbar = view.findViewById(R.id.seekBar)
        seekbar.progress = bitmapColorSpread.aCurrentRange.progressIncrement
        seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

        imageButton = view.findViewById(R.id.palette_scaler)

        isLinearView = view.findViewById(R.id.data_to_colour_type)

        setAnalysisButtonTitle()

        tileImageView = view.findViewById(R.id.tile_image_view)

        updateTextures()

        setTileViewBitmap(pixelDataClone)

        seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (jobTextures == null || jobTextures?.isActive == false) {
                        bitmapColorSpread.setProgress(seekBar.progress)
                        updateTextures()
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (jobTextures!= null && jobTextures?.isActive == true){
                        jobTextures?.cancel()
                    }

                    seekBar.progress.also { seekBar.secondaryProgress = it }
                    bitmapColorSpread.setProgress(seekBar.progress)
                    bitmapColorSpread.aCurrentRange.progressSecond = seekBar.progress

                    updateTextures()
                }

            }
        )

        view.findViewById<Button>(R.id.undo_all_changes).setOnClickListener {
            MainActivity.scopeIO.launch {
                pixelDataClone = pixelData.Clone()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.set_to_zero).setOnClickListener {
            MainActivity.scopeIO.launch {
                runSetToZero()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.data_to_colour_type).setOnClickListener {
            switchProcessType()

            seekbar.progress = bitmapColorSpread.getProgress()
            seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

            setAnalysisButtonTitle()

            updateTextures()
        }

        view.findViewById<Button>(R.id.blur_left).setOnClickListener {
            MainActivity.scopeIO.launch {
                blurLeft()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.blur_right).setOnClickListener {
            MainActivity.scopeIO.launch {
                blurRight()

                updateTextures()
            }
        }



        view.findViewById<Button>(R.id.palette_left).setOnClickListener {
            bitmapColorSpread.prevPalette()

            seekbar.progress = bitmapColorSpread.getProgress()
            seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

            setAnalysisButtonTitle()

            updateTextures()
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener {
            bitmapColorSpread.nextPalette()

            seekbar.progress = bitmapColorSpread.getProgress()
            seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

            setAnalysisButtonTitle()


            updateTextures()
        }



        view.findViewById<Button>(R.id.add_new_palette).setOnClickListener {
            bitmapColorSpread.addNewColorA()

            seekbar.progress = bitmapColorSpread.getProgress()
            seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

            setAnalysisButtonTitle()

            updateTextures()
        }

        view.findViewById<Button>(R.id.add_new_palette2).setOnClickListener {
            bitmapColorSpread.addNewColorB()

            seekbar.progress = bitmapColorSpread.getProgress()
            seekbar.secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

            setAnalysisButtonTitle()

            updateTextures()
        }


        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


        view.findViewById<Button>(R.id.to_savewallpaper).setOnClickListener {

            (this.activity as AppCompatActivity).supportActionBar?.hide()

            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }
    }

    private fun updateTextures() {
        jobTextures = CoroutineScope(Dispatchers.IO).launch {
            bitmapColorSpread.updateColorSpreadBitmap(pixelDataClone)

            CoroutineScope(Dispatchers.Main).launch {
                if (bitmapColorSpread.mNewColors == true) {
                    imageButton.setImageBitmap(bitmapColorSpread.seekbarBitmap)
                    bitmapColorSpread.mNewColors = false
                }
            }

            setTileViewBitmap(pixelDataClone)
        }
    }

    private fun setAnalysisButtonTitle(){
        val text : KotlinString
        if (bitmapColorSpread.aCurrentRange.dataProcess == DataProcess.LINEAR) {
            text = "Linear" } else {
            text = "Statistical" }
        isLinearView.text = text.subSequence(0, text.length)
    }

}