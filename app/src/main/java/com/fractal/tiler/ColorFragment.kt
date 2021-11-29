package com.fractal.tiler

// region Variable Declaration

import android.graphics.Color
import android.graphics.drawable.*
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
import com.fractal.tiler.MainActivity.Companion.myResources


class ColorImageButton(val button: ImageButton?, drawableLayerId : Int,
                       private val itemInLayerId: Int, val buttonId : Int, private val hasStateList : Boolean = false){

    private var layerBackground : LayerDrawable
    private var backgroundDrawable : Drawable
    private var backgroundColor = 0xff7f7f7f.toInt()

    private val layerForeground : LayerDrawable?


    init {
        layerBackground = ResourcesCompat.getDrawable(
            myResources, drawableLayerId, null) as LayerDrawable

        backgroundDrawable = backgroundColor.toDrawable()

        button?.setBackground(layerBackground)

        layerForeground = if (hasStateList){
            val foregroundStateList = button?.getForeground() as StateListDrawable
            val drawableContainerState = foregroundStateList.constantState as DrawableContainer.DrawableContainerState

            drawableContainerState.children[0] as LayerDrawable
        } else {
            null
        }

    }

    fun setButtonBackground(drawable: Drawable, activeButtonId : Int){
        backgroundDrawable = drawable

        layerBackground.setDrawableByLayerId(itemInLayerId, backgroundDrawable)

        button?.invalidate()
    }

    fun setButtonForeground(){

    }
}

class ColorImageButtonOld(val button: ImageButton?, val buttonId: Int, val isBookEnd: Boolean = false){
    // region Variable Declaration

    private var layerBackground : LayerDrawable
    private var backgroundDrawable : Drawable
    private var backgroundColor = 0xff7f7f7f.toInt()

    private var layerFromState_0 : LayerDrawable? = null
    private var layerFromState_1 : LayerDrawable? = null
    private var layerFromState_2 : LayerDrawable? = null

    // endregion


    init {
        layerBackground = ResourcesCompat.getDrawable(
            myResources, R.drawable.layer_background_color, null) as LayerDrawable

        backgroundDrawable = backgroundColor.toDrawable()

        val stateList = button?.background as StateListDrawable
        val containerState = stateList.constantState as DrawableContainer.DrawableContainerState
        val containerChildren = containerState.children

        layerFromState_0 = containerChildren[0] as LayerDrawable
        layerFromState_1 = containerChildren[1] as LayerDrawable
        layerFromState_2 = containerChildren[2] as LayerDrawable

        button.setBackground(stateList)

        //this.layer = ResourcesCompat.getDrawable(myResources, R.drawable.color_button_states, null) as LayerDrawable
    }

    fun setButtonColor(color : Int, activeButtonId : Int){
        backgroundColor = color
        backgroundDrawable = backgroundColor.toDrawable()

        layerFromState_0?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)
        layerFromState_1?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)
        layerFromState_2?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)

        button?.invalidate()
    }

    fun setButtonColor(drawable : Drawable, activeButtonId : Int){
        backgroundDrawable = drawable

        layerBackground.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)

        if (!isBookEnd) {
            layerFromState_0?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)
            layerFromState_1?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)
            layerFromState_2?.setDrawableByLayerId(R.id.layer_color, backgroundDrawable)
        }

        button?.invalidate()
    }
}

class ColorsRow(binding : FragmentColorBinding){

    // region Variable Declaration

    private var lastIndex = 4

    val colorButtons : Array<ColorImageButton>

    var activeButtonId = -1

    // endregion


    init{
        colorButtons = arrayOf(
            ColorImageButton(binding.startColor, R.drawable.layer_background_color, R.id.layer_color, 0),
            ColorImageButton(binding.color0,R.drawable.layer_background_color, R.id.layer_color,  1, true),
            ColorImageButton(binding.color1, R.drawable.layer_background_color, R.id.layer_color, 2, true),
            ColorImageButton(binding.color2, R.drawable.layer_background_color, R.id.layer_color, 3, true),
            ColorImageButton(binding.endColor, R.drawable.layer_background_color, R.id.layer_color, 4))

        setActiveButton(1)
    }

    fun setColors(primaryColors : IntArray, activeButtonId: Int) {
        lastIndex = primaryColors.lastIndex
        this.activeButtonId = activeButtonId

        colorButtons[0].setButtonBackground(primaryColors[0].toDrawable(),
            this.activeButtonId)
        colorButtons[4].setButtonBackground(primaryColors[lastIndex].toDrawable(),
            this.activeButtonId)

        for (i in 1..3){
            if (lastIndex > i) {
                colorButtons[i].setButtonBackground(primaryColors[i].toDrawable(),
                    this.activeButtonId)
            } else {
                colorButtons[i].setButtonBackground(Color.GRAY.toDrawable(),
                    this.activeButtonId)
            }
        }
    }

    fun setActiveButton(buttonId : Int){
        if (buttonId == activeButtonId) return

        if (activeButtonId > -1){
            val fr = ResourcesCompat.getDrawable(myResources, R.drawable.color_button_states, null) as StateListDrawable
            colorButtons[activeButtonId].button?.setForeground(fr)
        }

        activeButtonId = buttonId

        colorButtons[activeButtonId].button?.setForeground(
            ResourcesCompat.getDrawable(myResources, R.drawable.layer_button_color_pressed, null) as LayerDrawable)
    }
}

// endregion


class ColorFragment : Fragment() {

    // region Variable Declaration

    private var _fragmentColorBinding : FragmentColorBinding? = null
    private val binding get() = _fragmentColorBinding!!

    private var colorRangeRight : ImageButton? = null
    lateinit var colorRLayerRight : LayerDrawable
    lateinit var colorRDrawableRight : Drawable

    private var colorRangeLeft : ImageButton? = null
    private lateinit var colorRLayerLeft : LayerDrawable
    private lateinit var colorRDrawableLeft : Drawable

    private var colorRangeMid : TextView? = null
    private lateinit var colorRLayerMid : LayerDrawable
    private lateinit var colorRDrawableMid : Drawable

    private lateinit var colorsRow : ColorsRow

    private lateinit var seekbar: MySeekbar

    private var imageButton: ImageButton? = null
    private lateinit var imageButtonLayer : LayerDrawable
    private lateinit var imageButtonDrawable : GradientDrawable


    val mThisPageID = 1

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        _fragmentColorBinding = FragmentColorBinding.inflate(inflater, container, false)

        colorRLayerRight =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        colorRDrawableRight = colorClass.aPrevRange.colorRangeDrawable

        colorRLayerMid =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        colorRDrawableMid = colorClass.aCurrentRange.colorRangeDrawable

        //colorRLayerMid = colorRangeMid?.background as LayerDrawable
        val transStrokeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.shape_stroke_trans, null) as GradientDrawable
        colorRLayerMid.setDrawableByLayerId(R.id.layer_stroke, transStrokeDrawable)

        colorRLayerLeft =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_background_bitmap, null) as LayerDrawable
        colorRDrawableLeft = colorClass.aNextRange.colorRangeDrawable

        colorRangeRight = binding.palleft
        colorRangeRight?.setBackground(colorRLayerRight)
        setColorRangeRightBackground()

        colorRangeMid = binding.palmid
        colorRangeMid?.setForeground(colorRLayerMid)
        setColorRangeMidBackground()

        colorRangeLeft = binding.palright
        colorRangeLeft?.setBackground(colorRLayerLeft)
        setColorRangeLeftBackground()


        colorsRow = ColorsRow(binding)

        for (i in 0..2){
            colorsRow.colorButtons[i].button?.isVisible =
                (colorClass.aCurrentRange.mColorDataList.size > i + 2)
        }

        colorsRow.setColors(colorClass.aCurrentRange.primaryColors, colorsRow.activeButtonId)
        //colorsRow.colorButtons[colorClass.aCurrentRange.mActiveColorButtonId].button?.requestFocus()

        seekbar = binding.seekBar

        imageButton = binding.colorrangeImage
        imageButtonLayer =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_edit, null) as LayerDrawable

        imageButton?.setForeground(imageButtonLayer)

        imageButtonDrawable = imageButtonLayer.findDrawableByLayerId(R.id.gradient_edit) as GradientDrawable
        setColorButtons()

        //isLinearView = binding.dataColourConstraint.dataToColourType

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* isLinearView = view.findViewById(R.id.data_to_colour_type) */

        setAnalysisButtonTitle()

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
                            colorClass.setProgress(seekBar.progress)

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
                    colorClass.setProgress(seekBar.progress)
                    colorClass.aCurrentRange.progressSecond = seekBar.progress

                    if (!calcActive) {
                        updateTextures()
                    }

                    isStarted = false
                }

            } )

        colorsRow.colorButtons[1].button?.setOnClickListener {
            colorClass.aCurrentRange.mActiveColorButtonId = 1

            if (colorsRow.activeButtonId != 1) {
                setColorButtons()
            }
        }
        colorsRow.colorButtons[2].button?.setOnClickListener {
            colorClass.aCurrentRange.mActiveColorButtonId = 2

            if (colorsRow.activeButtonId != 2) {
                setColorButtons()
            }
        }
        colorsRow.colorButtons[3].button?.setOnClickListener {
            colorClass.aCurrentRange.mActiveColorButtonId = 3

            if (colorsRow.activeButtonId != 3) {
                setColorButtons()
            }
        }

        /*
        view.findViewById<Button>(R.id.data_to_colour_type).setOnClickListener {
            switchProcessType()

            seekbar.progress = MainActivity.colorClass.getProgress()
            seekbar.secondaryProgress = seekbar.progress

            setAnalysisButtonTitle()

            if (!calcActive) {
                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }
        */


        colorRangeRight?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectPrevColorRange()

                seekbar.progress = colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }

        colorRangeLeft?.setOnClickListener {
            if (!calcActive) {
                colorClass.selectNextColorRange()

                seekbar.progress = colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }

        view.findViewById<Button>(R.id.add_new_palette).setOnClickListener {
            if (!calcActive) {
                colorClass.addNewColorA()

                seekbar.progress = colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()
            }
        }

        val anphsv = view.findViewById<Button>(R.id.add_new_palettehsv)
        val anp2Layer =
            ResourcesCompat.getDrawable(resources, R.drawable.add_button_hsv_up, null) as LayerDrawable
        val draw2 = anp2Layer.findDrawableByLayerId(R.id.layer_color) as GradientDrawable
        draw2.colors = intArrayOf(0xffaf0000.toInt(), 0xff00af00.toInt(), 0xff0000af.toInt(), 0xffaf0000.toInt())
        anp2Layer.setDrawableByLayerId(R.id.layer_color, draw2)
        anphsv.setForeground(anp2Layer)

        anphsv.setOnClickListener {
            if (!calcActive) {
                colorClass.addNewColorB()

                seekbar.progress = colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }
    }


    private fun setColorRangeRightBackground(){
        colorRDrawableRight = colorClass.aPrevRange.colorRangeDrawable
        colorRLayerRight.setDrawableByLayerId(R.id.layer_bitmap, colorRDrawableRight)
        colorRangeRight?.invalidate()
    }
    private fun setColorRangeMidBackground(){
        colorRDrawableMid = colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources)
        colorRLayerMid.setDrawableByLayerId(R.id.layer_bitmap, colorRDrawableMid)
        colorRangeMid?.invalidate()
    }
    private fun setColorRangeLeftBackground(){
        colorRDrawableLeft = colorClass.aNextRange.colorRangeDrawable
        colorRLayerLeft.setDrawableByLayerId(R.id.layer_bitmap, colorRDrawableLeft)
        //colorRangeLeft?.setForeground(colorRLayerLeft)
        colorRangeLeft?.invalidate()
    }
    private fun setAllColorRangeBackgrounds(){
        setColorRangeRightBackground()
        setColorRangeMidBackground()
        setColorRangeLeftBackground()

        setColorButtons()
    }

    private fun setColorButtons(){
        val colorIndex = colorClass.aCurrentRange.mActiveColorButtonId
        val colorDataList = colorClass.aCurrentRange.mColorDataList

        colorsRow.setActiveButton(colorIndex)

        imageButtonDrawable.colors = intArrayOf(
            colorClass.aCurrentRange.primaryColors[colorIndex - 1],
            colorClass.aCurrentRange.primaryColors[colorIndex],
            colorClass.aCurrentRange.primaryColors[colorIndex + 1])

        val lhs = colorDataList[colorIndex - 1].range + 1
        val mid = colorDataList[colorIndex].range - lhs
        val max = colorDataList[colorIndex + 1].range - lhs

        val frac : Float = mid / max.toFloat()

        imageButtonDrawable.setGradientCenter(frac, 0.5f)

        imageButtonLayer.setDrawableByLayerId(R.id.gradient_edit, imageButtonDrawable)
        imageButton?.invalidate()
    }


    private fun updateTextures(setTileView: Boolean = true) {
        jobTextures = CoroutineScope(Dispatchers.IO).launch {

            colorClass.aCurrentRange.updateColorSpreadBitmap()

            CoroutineScope(Dispatchers.Main).launch {

                setAllColorRangeBackgrounds()

                val lastIndex = colorClass.aCurrentRange.mColorDataList.lastIndex
                for (i in 1..3){
                    colorsRow.colorButtons[i].button?.isVisible = (lastIndex > i)
                }

                //colorsRow.colorButtons[colorClass.aCurrentRange.mActiveColorButtonId].button?.requestFocus()

                colorsRow.setColors(colorClass.aCurrentRange.primaryColors, 1)

                if (colorClass.mNewColors) {
                    setColorButtons()
                    colorClass.mNewColors = false
                }
            }

            if (setTileView) setTileViewBitmap(pixelDataClone)
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
}