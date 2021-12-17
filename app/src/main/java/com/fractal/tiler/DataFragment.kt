package com.fractal.tiler

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.databinding.FragmentDataBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class DataFragment : Fragment() {

    // region Variable Declaration

    private var _fragmentDataBinding : FragmentDataBinding? = null
    private val binding get() = _fragmentDataBinding!!

    private val seekbarBitmapWidth = 512
    private var aBitmapColors = IntArray(seekbarBitmapWidth)

    private lateinit var seekbar: MySeekbar
    private lateinit var seekbarBackground : LayerDrawable
    private val seekbarBitmap : Bitmap = Bitmap.createBitmap(seekbarBitmapWidth, 1, Bitmap.Config.ARGB_8888)
    private var seebarDrawable = seekbarBitmap.toDrawable(MainActivity.myResources)

    private var mMaxSeekbarHit = seekbarBitmapWidth - 1
    private var mSeekbarProgess = mMaxSeekbarHit

    private var mOneOverWidth = 1.0f / mMaxSeekbarHit

    var filterId = filter.ordinal

    var calcActive = false

    var jobTextures : Job? = null

    // endregion


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aBitmapColors.fill(0xffa0a0a0.toInt(), 0, seekbarBitmapWidth - 1)

        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _fragmentDataBinding = FragmentDataBinding.inflate(inflater, container, false)

        if (MainActivity.mEnableDataClone) {
            pixelDataClone = pixelData.clone()
            MainActivity.mEnableDataClone = false
        }

        seekbar = binding.maxhitSeeker
        setSeekbarProgressAndMax()

        seekbarBackground =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable

        setSeekbarBitmapColors()

        seekbar.setBackground(seekbarBackground)
        seekbar.invalidate()

        //seekbarBackground.setDrawableByLayerId(R.id.layer_bitmap, seebarDrawable)

        mMaxSeekbarHit = pixelDataClone.mMaxHits

        seekbar.max = mMaxSeekbarHit

        seekbar.progress = mMaxSeekbarHit

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

                updateTextures()

                isStarted = false
            }

        })

    }


    private fun updateTextures(setTileView: Boolean = true) {

        var doSetTileView = setTileView

        jobTextures = CoroutineScope(Dispatchers.Default).launch {
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
        seekbar.progress = mSeekbarProgess
        seekbar.max = mMaxSeekbarHit
    }

    private fun setSeekbarBitmapColors(){
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
        var gb : Int
        var shader = 1.0f
        val mult = pixelDataClone.mMaxHits / (seekbarBitmapWidth - 1).toFloat()

        //todo stretch mMaxHitValue across bitmapWidth
        for (i in 0 until seekbarBitmapWidth) {
            iscaled = (i * mult).toInt()

            if (iscaled >= mSeekbarProgess) {
                alpha = 32
                shader = 0.0f
            }

            r = (aPercentOfTotalHits[iscaled])
            gb = (r * shader).toInt()

            aBitmapColors[i] = Color.argb(alpha, r, r, r)
        }

        seekbarBitmap.setPixels(aBitmapColors, 0, seekbarBitmapWidth, 0, 0, seekbarBitmapWidth, 1)
        seekbarBackground.setDrawableByLayerId(R.id.layer_bitmap, seekbarBitmap.toDrawable(resources))
    }

    override fun onResume() {
        super.onResume()

        val filters = resources.getStringArray(R.array.filter_types)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_filter, filters)

        binding.autotextview.setAdapter(arrayAdapter)

        val text = arrayAdapter.getItem(filterId).toString()

        binding.autotextview.setText(text, false)

        binding.autotextview.setListSelection(filterId)

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
        _fragmentDataBinding = null
    }

}

