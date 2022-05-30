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
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.colorClass
import com.fractal.tiler.MainActivity.Companion.dpToPx
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.databinding.FragmentDataBinding
import kotlinx.coroutines.*
import kotlin.math.atan


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
    private lateinit var roundedBitmapDrawable : RoundedBitmapDrawable

    private var mMaxSeekbarHit = 0
    private var mSeekbarProgess = 0

    var filterId = filter.ordinal

    var doUndoChanges = false
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
            ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable

      //  pixelDataClone.calcTangentScale()
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
                doUndoChanges = true

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

                    MainActivity.dataFragmentSeekbarProgress = 1.0f - (mSeekbarProgess / mMaxSeekbarHit.toFloat())

                    if (jobTextures == null || jobTextures?.isActive == false) {

                        if (!calcActive) {
                            pixelDataClone.calcTangentScale()

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

                MainActivity.dataFragmentSeekbarProgress = 1.0f - (mSeekbarProgess / mMaxSeekbarHit.toFloat())

                pixelDataClone.calcTangentScale()

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
                        Blur.doHitsFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Gaussian -> {
                        Gaussian.doHitsFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Motion -> {
                        Motion.doHitsFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.BoxBlur -> {
                        Sharpen.doHitsFilter(pixelDataClone)
                    }
                    MainActivity.Companion.ImageFilter.Median -> {
                        Smooth.doHitsFilter(pixelDataClone)
                    }
                }

                pixelDataClone.calcTangentScale()

                setSeekbarProgressAndMax()

                doFilter = false
            }

            if (doUndoChanges){
                pixelDataClone = pixelData.clone()
                //pixelDataClone.recalcScaledHitStats()
                pixelDataClone.calcTangentScale()

                setSeekbarProgressAndMax()

                doUndoChanges = false
            }

            setSeekbarBitmapColors()

            seekbar.invalidate()

            if (doSetTileView) {
                calcActive = true

                setTileViewBitmap(pixelDataClone, true)

                calcActive = false
            }
        }
    }

    private fun setSeekbarProgressAndMax(){
        mMaxSeekbarHit = pixelDataClone.mMaxHits
        mSeekbarProgess = (mMaxSeekbarHit * (1.0f - MainActivity.dataFragmentSeekbarProgress)).toInt()
        seekbar.max = mMaxSeekbarHit
        seekbar.progress = mSeekbarProgess
    }


    private fun setSeekbarBitmapColors(){
        val maxAngle = 0.4 + (1.15 * MainActivity.dataFragmentSeekbarProgress)
        val mult = Math.tan(maxAngle) / (seekbarBitmapWidth - 1)
        val colmult = MainActivity.mColorRangeLastIndex / maxAngle

        var r : Int
        for (i in 0 until seekbarBitmapWidth) {
            r = (atan(i * mult) * colmult).toInt()

            aBitmapColors[i] = colorClass.aCurrentRange.aColorSpread[r]//Color.argb(255, 15 + r, 15 + r, r)
        }

        seekbarBitmap.setPixels(aBitmapColors, 0, seekbarBitmapWidth, 0, 0, seekbarBitmapWidth, 1)
        seekbarBackground.setDrawableByLayerId(R.id.layer_bitmap, getRoundedBitmap(seekbarBitmap))
    }

    private fun getRoundedBitmap(bitmap : Bitmap) : RoundedBitmapDrawable {
        roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedBitmapDrawable.cornerRadius = dpToPx

        return roundedBitmapDrawable
    }

    override fun onResume() {
        super.onResume()

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
            _: AdapterView<*>?,
            _: View?,
            position: Int,
            _: Long ->
                filter = MainActivity.Companion.ImageFilter.values()[position]

                filterId = position
        }

        setSeekbarBitmapColors()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        MainActivity.dataFragmentSeekbarProgress = 1.0f - (mSeekbarProgess / mMaxSeekbarHit.toFloat())

        _fragmentDataBinding = null
    }

}


