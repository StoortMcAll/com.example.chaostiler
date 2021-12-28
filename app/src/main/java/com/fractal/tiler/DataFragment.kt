package com.fractal.tiler

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.databinding.FragmentDataBinding
import kotlinx.coroutines.*


class DataFragment : Fragment() {

    // region Variable Declaration

    private var _fragmentDataBinding : FragmentDataBinding? = null
    private val binding get() = _fragmentDataBinding!!

    private val seekbarBitmapWidth = 512

    private var aBitmapColors = IntArray(seekbarBitmapWidth)

    private lateinit var undoChangesButton : Button

    private lateinit var applyFilterButton : Button
    private lateinit var filterListView : AutoCompleteTextView

    private lateinit var filters : Array<String> //= resources.getStringArray(R.array.filter_types)

    private lateinit var seekbar: MySeekbar
    private lateinit var seekbarBackground : LayerDrawable
    private val seekbarBitmap : Bitmap = Bitmap.createBitmap(seekbarBitmapWidth, 1, Bitmap.Config.ARGB_8888)

    private var mMaxSeekbarHit = 0
    private var mSeekbarProgess = 0

    var filterId = filter.ordinal

    var doFilter = false
    var calcActive = false

    var jobTextures : Job? = null

    // endregion


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _fragmentDataBinding = FragmentDataBinding.inflate(inflater, container, false)

        seekbar = binding.maxhitSeeker

        setSeekbarProgressAndMax()

        seekbarBackground =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable

        setSeekbarBitmapColors()

        seekbar.setBackground(seekbarBackground)
        seekbar.invalidate()

        undoChangesButton = binding.undoChanges

        applyFilterButton = binding.applySmooth

        filterListView = binding.autotextview

        filters = resources.getStringArray(R.array.filter_types)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_filter, filters)

        filterListView.setAdapter(arrayAdapter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoChangesButton.setOnClickListener {
            if (jobTextures == null || jobTextures?.isActive == false) {
                pixelDataClone = pixelData.clone()

                setSeekbarProgressAndMax()

                updateTextures()
            }
        }

        applyFilterButton.setOnClickListener {
            if (jobTextures == null || jobTextures?.isActive == false) {
                doFilter = true

                updateTextures()
            }
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var isStarted = false

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!isStarted) {
                    if (fromUser) {
                        isStarted = true
                    }
                } else {
                    mSeekbarProgess = progress
                    if (mSeekbarProgess < pixelDataClone.mMinHits)
                        mSeekbarProgess = pixelDataClone.mMinHits

                    seekBar.progress = progress

                    MainActivity.dataFragmentSeekbarProgress = mSeekbarProgess / mMaxSeekbarHit.toFloat()

                    if (jobTextures == null || jobTextures?.isActive == false) {

                        if (!calcActive) {
                            updateTextures()
                        }
                    }

                    return
                }

                if (isStarted) {
                    mSeekbarProgess = progress
                    if (mSeekbarProgess < pixelDataClone.mMinHits)
                        mSeekbarProgess = pixelDataClone.mMinHits

                    seekBar.progress = progress
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (jobTextures != null && jobTextures?.isActive == true) {
                    jobTextures?.cancel()
                }

                mSeekbarProgess = seekBar.progress
                if (mSeekbarProgess < pixelDataClone.mMinHits)
                    mSeekbarProgess = pixelDataClone.mMinHits

                MainActivity.dataFragmentSeekbarProgress = mSeekbarProgess / mMaxSeekbarHit.toFloat()

                updateTextures()

                isStarted = false
            }

        })

    }


    private fun updateTextures(setTileView: Boolean = true) {

        var doSetTileView = setTileView

        jobTextures = CoroutineScope(Dispatchers.Default).launch {

            if (doFilter) {
                when (filter) {
                    MainActivity.Companion.ImageFilter.Blur -> {
                        Blur.doImageFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Gaussian -> {
                        Gaussian.doImageFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Motion -> {
                        Motion.doImageFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.BoxBlur -> {
                        BoxBlur.doImageFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Median -> {
                        BoxBlur.doImageFilter(pixelDataClone)
                    }
                }

                setSeekbarProgressAndMax()

                doFilter = false
            }

            setSeekbarBitmapColors()

            seekbar.invalidate()

            if (doSetTileView) {
                calcActive = true

                pixelDataClone.recalcScaledHitsArray(mSeekbarProgess)

                setTileViewBitmap(pixelDataClone)

                calcActive = false
            }
        }
    }

    private fun setSeekbarProgressAndMax(){
        mMaxSeekbarHit = pixelDataClone.mMaxHits
        mSeekbarProgess = (mMaxSeekbarHit * MainActivity.dataFragmentSeekbarProgress).toInt()
        seekbar.max = mMaxSeekbarHit
        seekbar.progress = mSeekbarProgess
    }


    private fun setSeekbarBitmapColors(){
        val maxAngle = 0.4 + (1.1707 * (mSeekbarProgess / mMaxSeekbarHit.toDouble()))
        val maxTan = Math.tan(maxAngle)
        val mult = maxTan / seekbarBitmapWidth
        val colmult = 255.0 / maxAngle
        var r : Int

        for (i in 0 until seekbarBitmapWidth) {
            r = (Math.atan(i * mult) * colmult).toInt()

            aBitmapColors[i] = Color.argb(255, r, r, r)
        }

        seekbarBitmap.setPixels(aBitmapColors, 0, seekbarBitmapWidth, 0, 0, seekbarBitmapWidth, 1)
        seekbarBackground.setDrawableByLayerId(R.id.layer_bitmap, seekbarBitmap.toDrawable(resources))
    }


    private fun setSeekbarBitmapColors2(){
        val aPercentOfTotalHits = IntArray(pixelDataClone.mMaxHits + 1)
        val mTotal = 255.0f / pixelDataClone.arraySize

        var mRunningTotal = 0

        for (i in 0..pixelDataClone.mMaxHits){
            mRunningTotal += pixelDataClone.aHitStats[i]
            aPercentOfTotalHits[i] = (mRunningTotal * mTotal).toInt()
        }

        var iscaled : Int
        var alpha = 255
        var r : Int
        val mult = pixelDataClone.mMaxHits / (seekbarBitmapWidth - 1).toFloat()

        for (i in 0 until seekbarBitmapWidth) {
            iscaled = (i * mult).toInt()

            if (iscaled >= mSeekbarProgess) {
                alpha = 160
            }

            r = (aPercentOfTotalHits[iscaled]).and(248)

            aBitmapColors[i] = Color.argb(alpha, r, r, 255)
        }

        seekbarBitmap.setPixels(aBitmapColors, 0, seekbarBitmapWidth, 0, 0, seekbarBitmapWidth, 1)
        seekbarBackground.setDrawableByLayerId(R.id.layer_bitmap, seekbarBitmap.toDrawable(resources))
    }

    override fun onResume() {
        super.onResume()

       // val filters = resources.getStringArray(R.array.filter_types)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_filter, filters)

        filterListView.setAdapter(arrayAdapter)

        val text = arrayAdapter.getItem(filterId).toString()

        filterListView.setText(text, false)

        filterListView.setListSelection(filterId)

     /*   binding.autotextview.onItemClickListener = object :
            AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long, ) {
            }
        }
        */

        binding.autotextview.setOnItemClickListener {
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long ->
                filter = MainActivity.Companion.ImageFilter.values()[position]

                filterId = position
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        MainActivity.dataFragmentSeekbarProgress = mSeekbarProgess / mMaxSeekbarHit.toFloat()

        _fragmentDataBinding = null
    }

}


