package com.fractal.tiler

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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

    private var _fragmentDataBinding : FragmentDataBinding? = null
    private val binding get() = _fragmentDataBinding!!


    private lateinit var seekbar: MySeekbar
    private lateinit var seebarBackground : LayerDrawable
    private val seebarBitmap : Bitmap = Bitmap.createBitmap(MainActivity.mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    private var seebarDrawable = seebarBitmap.toDrawable(MainActivity.myResources)

    private var mMaxSeekbarHit = 100
    private var mSeekbarProgess = 100

    var filterId = filter.ordinal


    var calcActive = false

    var jobTextures : Job? = null

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

        if (MainActivity.mEnableDataClone) {
            pixelDataClone = pixelData.clone()
            MainActivity.mEnableDataClone = false
        }

        seekbar = binding.maxhitSeeker

        seebarBackground =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_edit, null) as LayerDrawable

        mMaxSeekbarHit = pixelDataClone.mMaxHits

        seekbar.max = mMaxSeekbarHit

        seekbar.progress = mMaxSeekbarHit



/*

        val gradient = GradientDrawable()

        gradient.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        gradient.shape= GradientDrawable.RECTANGLE
        gradient.colors = intArrayOf(Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.GREEN)
        gradient.cornerRadius = 8.0F
        gradient.setStroke(2, resources.getColor(R.color.button_stroke))

        val button : Button = binding.undoChanges
        button.foreground =  gradient
*/

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

                if (!calcActive) {
                    updateTextures()
                }

                isStarted = false
            }

        })

    }


    private fun updateTextures(setTileView: Boolean = true) {

        var doSetTileView = setTileView

        jobTextures = CoroutineScope(Dispatchers.Default).launch {

            if (doSetTileView) {
                pixelDataClone.recalcScaledHitsArray(mSeekbarProgess)

                setTileViewBitmap(pixelDataClone)

            }
        }
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

