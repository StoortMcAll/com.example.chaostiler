package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.databinding.FragmentColorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import com.fractal.tiler.MainActivity.Companion.colorClass

// endregion


class ColorFragment : Fragment() {

    // region Variable Declaration

    private var _fragmentColorBinding : FragmentColorBinding? = null
    private val binding get() = _fragmentColorBinding!!

    private var selectPrevColorRange : ImageButton? = null
    lateinit var prevRangeLayer : LayerDrawable
    lateinit var prevRangeDrawable : Drawable

    private var selectNextColorRange : ImageButton? = null
    private lateinit var nextRangeLayer : LayerDrawable
    private lateinit var nextRangeDrawable : Drawable

    private var colorRangeMid : TextView? = null
    private lateinit var midRangeLayer : LayerDrawable
    private lateinit var colorRDrawableMid : Drawable

    private val hsvWheelColors = intArrayOf(0xffaf0000.toInt(), 0xff00af00.toInt(), 0xff0000af.toInt(), 0xffaf0000.toInt())

    private lateinit var colorsRow : ColorsRow

    private lateinit var seekbar: MySeekbar
    private lateinit var seekbarLayer: LayerDrawable

    lateinit var roundedBitmapDrawable: RoundedBitmapDrawable

    val dpToPx = MainActivity.dpToPx

    val mThisPageID = 1

    var calcActive = false

    var newColorRange = true
    var newRangeProgress = false

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        _fragmentColorBinding = FragmentColorBinding.inflate(inflater, container, false)

        colorsRow = ColorsRow(binding, colorClass.aCurrentRange.aRgbColorsList)

        colorsRow.setActiveButton(colorClass.aCurrentRange.mActiveColorIndex)

        prevRangeLayer = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable

        midRangeLayer = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_trans_stroke, null) as LayerDrawable

        nextRangeLayer = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable


        seekbarLayer = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable
        seekbarLayer.mutate()

        selectPrevColorRange = binding.palleft
        colorRangeMid = binding.palmid
        selectNextColorRange = binding.palright

        seekbar = binding.seekBar
        setSeekbarValues()

        setAllColorRangeBackgrounds(false)

        selectPrevColorRange?.setBackground(prevRangeLayer)
        colorRangeMid?.setForeground(midRangeLayer)
        selectNextColorRange?.setBackground(nextRangeLayer)
        seekbar.setBackground(seekbarLayer)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTextures(false)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var isStarted = false

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!isStarted) {
                    if (fromUser) {
                        isStarted = true
                    }
                } else {
                    seekBar.progress = progress
                    if (jobTextures == null || jobTextures?.isActive == false) {
                        colorClass.setProgress(progress)

                        if (!calcActive) {
                            //setColorButtons()
                            newRangeProgress = true
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

                colorClass.setProgress(seekBar.progress)

                if (!calcActive) {
                    //setColorButtons()
                    newRangeProgress = true
                    updateTextures()
                }

                isStarted = false
            }

        } )

        colorsRow.colorButtons[1].button?.setOnLongClickListener{
            true
        }
        colorsRow.colorButtons[1].button?.setOnClickListener{
            colorButtonClicked(1)
        }
        colorsRow.colorButtons[2].button?.setOnClickListener{
            colorButtonClicked(2)
        }
        colorsRow.colorButtons[3].button?.setOnClickListener{
            colorButtonClicked(3)
        }

        selectPrevColorRange?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectPrevColorRange()
                setSeekbarValues()

                setSeekbarFromActiveButtonBitmap()

                newColorRange = true

                updateTextures()
            }
        }
        selectNextColorRange?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectNextColorRange()
                setSeekbarValues()

                setSeekbarFromActiveButtonBitmap()

                newColorRange = true

                updateTextures()
            }
        }

        view.findViewById<ImageButton>(R.id.add_new_palette).setOnClickListener {
            if (!calcActive) {
                colorClass.addNewColorB()

                setSeekbarValues()

                newColorRange = true

                updateTextures()
            }
        }

        val addNewHsvButton = view.findViewById<ImageButton>(R.id.add_new_palettehsv)
        val addNewHsvLayer =
            ResourcesCompat.getDrawable(resources, R.drawable.add_button_hsv_up, null) as LayerDrawable
        val gradientDrawable = addNewHsvLayer.findDrawableByLayerId(R.id.layer_color) as GradientDrawable
        gradientDrawable.colors = hsvWheelColors

        addNewHsvLayer.setDrawableByLayerId(R.id.layer_color, gradientDrawable)
        addNewHsvButton.setForeground(addNewHsvLayer)

        addNewHsvButton.setOnClickListener {
            if (!calcActive) {
                colorClass.addNewColorA()

                setSeekbarValues()

                newColorRange = true

                updateTextures()
            }
        }
    }


    private fun colorButtonClicked(id : Int){

        if (colorsRow.activeButtonId != id) {
            colorClass.aCurrentRange.setActiveColorId(colorsRow.buttonGroup.group[id].colorIndex)
            setSeekbarValues()
            colorsRow.setActiveButton(id)

            newRangeProgress = true
            colorClass.mNewColors = false

            updateTextures(false)
        }

    }

    private fun getRoundedBitmap(bitmap : Bitmap) : RoundedBitmapDrawable {
        roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedBitmapDrawable.cornerRadius = dpToPx

        return roundedBitmapDrawable
    }

    private fun setSeekbarValues(){
        seekbar.max = colorClass.aCurrentRange.seekbarMax
        seekbar.progress = colorClass.aCurrentRange.progressIncrement

    }

    private fun setPrevColorRangeBackground(invalid : Boolean = true){
        //prevRangeDrawable = colorClass.aPrevRange.colorRangeDrawable
        prevRangeLayer.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(colorClass.aPrevRange.colorRangeBitmap)
        )

        if (invalid)
            selectPrevColorRange?.invalidate()
    }
    private fun setColorRangeMidBackground(invalid : Boolean = true){
        //colorRDrawableMid = colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources)
        midRangeLayer.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(colorClass.aCurrentRange.colorRangeBitmap)
        )

        if (invalid) colorRangeMid?.invalidate()
    }
    private fun setNextColorRangeBackground(invalid : Boolean = true){
        //nextRangeDrawable = colorClass.aNextRange.colorRangeDrawable
        nextRangeLayer.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(colorClass.aNextRange.colorRangeBitmap)
        )

        if (invalid) selectNextColorRange?.invalidate()
    }

    private fun setSeekbarFromActiveButtonBitmap(invalid : Boolean = true) {
        seekbarLayer.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(colorClass.aCurrentRange.colorButtonBitmap)
        )

        if (invalid) seekbar.invalidate()
    }

    private fun setAllColorRangeBackgrounds(invalid : Boolean = true){
        setPrevColorRangeBackground(invalid)
        setColorRangeMidBackground(invalid)
        setNextColorRangeBackground(invalid)

        setSeekbarFromActiveButtonBitmap(invalid)
    }

    private fun updateTextures(setTileView: Boolean = true) {

        var doSetTileView = setTileView

        jobTextures = CoroutineScope(Dispatchers.Default).launch {

            CoroutineScope(Dispatchers.Main).launch {

                if (newRangeProgress) {
                    setSeekbarFromActiveButtonBitmap()
                    setColorRangeMidBackground()

                    doSetTileView = true
                    colorClass.mNewColors = false
                    newRangeProgress = false
                }
                else if (colorClass.mNewColors) {
                    setAllColorRangeBackgrounds()

                    colorsRow.setColors(colorClass.aCurrentRange.aRgbColorsList, colorClass.aCurrentRange.mActiveColorIndex)

                    colorClass.mNewColors = false
                }

            }

            if (doSetTileView) setTileViewBitmap(pixelDataClone, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentColorBinding = null
    }

}