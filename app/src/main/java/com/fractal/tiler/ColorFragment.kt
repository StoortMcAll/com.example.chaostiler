package com.fractal.tiler

// region Variable Declaration

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
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
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
    private lateinit var seebarBackground : LayerDrawable

   // private lateinit var imageButtonDrawable : GradientDrawable


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

        prevRangeLayer =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        prevRangeDrawable = colorClass.aPrevRange.colorRangeDrawable

        midRangeLayer =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        colorRDrawableMid = colorClass.aCurrentRange.colorRangeDrawable

        //colorRLayerMid = colorRangeMid?.background as LayerDrawable
        val transStrokeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.shape_stroke_trans, null) as GradientDrawable
        midRangeLayer.setDrawableByLayerId(R.id.layer_stroke, transStrokeDrawable)

        nextRangeLayer =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        nextRangeDrawable = colorClass.aNextRange.colorRangeDrawable

        selectPrevColorRange = binding.palleft
        selectPrevColorRange?.setBackground(prevRangeLayer)
        setPrevColorRangeBackground()

        colorRangeMid = binding.palmid
        colorRangeMid?.setForeground(midRangeLayer)
        setColorRangeMidBackground()

        selectNextColorRange = binding.palright
        selectNextColorRange?.setBackground(nextRangeLayer)
        setNextColorRangeBackground()


        colorsRow = ColorsRow(binding, colorClass.aCurrentRange.mActiveColorButtonId)
/*

        for (i in 0..2){
            colorsRow.colorButtons[i].button?.isVisible =
                (colorClass.aCurrentRange.mColorDataList.size > i + 2)
        }
*/

        colorsRow.setColors(colorClass.aCurrentRange.aDataListColors, colorsRow.activeButtonId)

        seekbar = binding.seekBar
        setSeekbarValues()

        seebarBackground =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        seekbar.setBackground(seebarBackground)

        setSeekbarFromActiveButtonBitmap()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* isLinearView = view.findViewById(R.id.data_to_colour_type) */

        setAnalysisButtonTitle()
        //setColorButtons()
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

                //seekBar.progress.also { seekBar.secondaryProgress = it }
                colorClass.setProgress(seekBar.progress)
                //colorClass.aCurrentRange.progressSecond = seekBar.progress

                if (!calcActive) {
                    //setColorButtons()
                    newRangeProgress = true
                    updateTextures()
                }

                isStarted = false
            }

        } )

        colorsRow.colorButtons[1].button?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                colorClass.aCurrentRange.setActiveColorId(1)
                setSeekbarValues()
                if (colorsRow.activeButtonId != 1) {
                    colorsRow.setActiveButton(1)

                    //setColorButtons()
                    newRangeProgress = true
                    colorClass.mNewColors = false

                    updateTextures(false)
                    //setColorButtons()
                }
            }
        }
        colorsRow.colorButtons[2].button?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                colorClass.aCurrentRange.setActiveColorId(2)
                setSeekbarValues()
                if (colorsRow.activeButtonId != 2) {
                    colorsRow.setActiveButton(2)

                    //setColorButtons()
                    newRangeProgress = true
                    colorClass.mNewColors = false

                    updateTextures(false)
                    //setColorButtons()
                }
            }
        }
        colorsRow.colorButtons[3].button?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                colorClass.aCurrentRange.setActiveColorId(3)
                setSeekbarValues()
                if (colorsRow.activeButtonId != 3) {
                    colorsRow.setActiveButton(3)

                    //setColorButtons()
                    newRangeProgress = true
                    colorClass.mNewColors = false

                    updateTextures(false)
                    //setColorButtons()
                }
            }
        }

        selectPrevColorRange?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectPrevColorRange()
                setSeekbarValues()

                //colorsRow.setActiveButton(colorClass.aCurrentRange.mActiveColorButtonId)

                setSeekbarFromActiveButtonBitmap()
                setAnalysisButtonTitle()

                newColorRange = true

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }
        selectNextColorRange?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectNextColorRange()
                setSeekbarValues()

                //colorsRow.setActiveButton(colorClass.aCurrentRange.mActiveColorButtonId)

                setSeekbarFromActiveButtonBitmap()

                //setColorButtons()

                setAnalysisButtonTitle()

                newColorRange = true

                updateTextures()
            }
        }

        view.findViewById<ImageButton>(R.id.add_new_palette).setOnClickListener {
            if (!calcActive) {
                colorClass.addNewColorB()

                setSeekbarValues()

                setAnalysisButtonTitle()

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

                setAnalysisButtonTitle()

                newColorRange = true

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }
    }


    private fun setPrevColorRangeBackground(){
        prevRangeDrawable = colorClass.aPrevRange.colorRangeDrawable
        prevRangeLayer.setDrawableByLayerId(R.id.layer_bitmap, prevRangeDrawable)
        selectPrevColorRange?.invalidate()
    }
    private fun setColorRangeMidBackground(){
        colorRDrawableMid = colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources)
        midRangeLayer.setDrawableByLayerId(R.id.layer_bitmap, colorRDrawableMid)
        colorRangeMid?.invalidate()
    }
    private fun setNextColorRangeBackground(){
        nextRangeDrawable = colorClass.aNextRange.colorRangeDrawable
        nextRangeLayer.setDrawableByLayerId(R.id.layer_bitmap, nextRangeDrawable)
        selectNextColorRange?.invalidate()
    }
    private fun setAllColorRangeBackgrounds(){
        setPrevColorRangeBackground()
        setColorRangeMidBackground()
        setNextColorRangeBackground()

        setSeekbarFromActiveButtonBitmap()
    }

    private fun setSeekbarValues(){
        seekbar.progress = colorClass.aCurrentRange.progressIncrement
        seekbar.max = colorClass.aCurrentRange.seekbarMax
    }

    private fun setSeekbarFromActiveButtonBitmap() {
        seebarBackground.setDrawableByLayerId(R.id.layer_bitmap, colorClass.aCurrentRange.activeButtonDrawable)
        seekbar?.invalidate()
    }

    private fun updateTextures(setTileView: Boolean = true) {

        var doSetTileView = setTileView

        jobTextures = CoroutineScope(Dispatchers.Default).launch {

            //colorClass.aCurrentRange.updateColorSpreadBitmap()

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

                  /*  val lastIndex = colorClass.aCurrentRange.mColorDataList.lastIndex
                    for (i in 1..3) {
                        colorsRow.colorButtons[i].button?.isVisible = (lastIndex > i)
                    }*/

                    //colorsRow.colorButtons[colorClass.aCurrentRange.mActiveColorButtonId].button?.requestFocus()

                    colorsRow.setColors(colorClass.aCurrentRange.aDataListColors, colorClass.aCurrentRange.mActiveColorButtonId)


                    //if (colorClass.mNewColors) {

                        colorClass.mNewColors = false
                }

              //  } else if (newRangeProgress){

               // }
            }

            if (doSetTileView) setTileViewBitmap(pixelDataClone)
        }
    }

    private fun setAnalysisButtonTitle(){
        val text : String = if (colorClass.aCurrentRange.dataProcess == MainActivity.Companion.DataProcess.LINEAR) {
            "Linear"
        } else {
            "Statistical"
        }
       //isLinearView.text = text.subSequence(0, text.length)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentColorBinding = null
    }

}