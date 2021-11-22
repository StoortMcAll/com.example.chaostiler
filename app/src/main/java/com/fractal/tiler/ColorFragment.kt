package com.fractal.tiler

// region Variable Declaration

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
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

class ColorTextView(resources : Resources, textView : TextView?){
    val textView : TextView?
    var layer : LayerDrawable
    var color : Int = 0

    init {
        this.textView = textView
        this.layer = ResourcesCompat.getDrawable(resources, R.drawable.layer_textview_color, null) as LayerDrawable
        this.textView?.setForeground(this.layer)
    }

    fun setTextViewColor(color : Int){
        this.color = color

        layer.setDrawableByLayerId(R.id.layer_color, this.color.toDrawable())

        textView?.invalidate()
    }
}

class ColorButton(resources : Resources, button : Button?, buttonId : Int){
    val button : Button?
    val buttonId : Int

    var layer0 : LayerDrawable
    var layer1 : LayerDrawable
    var layer2 : LayerDrawable

    var color : Int = 0

    init {
        this.button = button
        this.buttonId = buttonId
        val stateList = button?.foreground as StateListDrawable
        val containerState = stateList.constantState as DrawableContainer.DrawableContainerState
        val children = containerState!!.children

        layer0 = children[0] as LayerDrawable
        layer1 = children[1] as LayerDrawable
        layer2 = children[2] as LayerDrawable

        //this.layer = ResourcesCompat.getDrawable(resources, R.drawable.color_button_states, null) as LayerDrawable
        this.button?.setForeground(stateList)
    }

    fun setButtonColor(color : Int, activeButtonId : Int){
        this.color = color

        //if (buttonId == activeButtonId)
            layer0.setDrawableByLayerId(R.id.layer_color, this.color.toDrawable())
            layer1.setDrawableByLayerId(R.id.layer_color, this.color.toDrawable())
            layer2.setDrawableByLayerId(R.id.layer_color, this.color.toDrawable())

        //else

        button?.invalidate()
    }
}

class ColorsRow(resources : Resources, binding : FragmentColorBinding){

    // region Variable Declaration

    var lastIndex = 2

    private var startColorView : ColorTextView
    private var endColorView : ColorTextView

    val colorButtons : Array<ColorButton>

    val activeButton = 0

    // endregion


    init{
        //val primaryColors = MainActivity.colorClass.aCurrentRange.primaryColors

        startColorView = ColorTextView(resources, binding.startColor)
        endColorView = ColorTextView(resources, binding.endColor)

        colorButtons = arrayOf(
            ColorButton(resources, binding.color0, 0),
            ColorButton(resources, binding.color1, 1),
            ColorButton(resources, binding.color2, 2))
    }

    fun setColors(primaryColors : IntArray) {
        lastIndex = primaryColors.lastIndex

        startColorView.setTextViewColor(primaryColors[0])
        endColorView.setTextViewColor(primaryColors[lastIndex])

        for (i in 0..2){
            if (lastIndex > i + 1) {
                colorButtons[i].setButtonColor(primaryColors[i + 1], i)
                //colorButtons[i].button?.isVisible = true
            } else {
                colorButtons[i].setButtonColor(Color.BLACK, i)
                //colorButtons[i]?.button?.isVisible = false
            }
        }
    }
}

// endregion


class ColorFragment : Fragment() {

    // region Variable Declaration

    private var _fragmentColorBinding : FragmentColorBinding? = null
    private val binding get() = _fragmentColorBinding!!

    private lateinit var colorsRow : ColorsRow

    private var colorRangeRight : Button? = null
    lateinit var colorRLayerRight : LayerDrawable
    lateinit var colorRDrawableRight : Drawable

    private var colorRangeLeft : Button? = null
    private lateinit var colorRLayerLeft : LayerDrawable
    private lateinit var colorRDrawableLeft : Drawable

    private var colorRangeMid : TextView? = null
    private lateinit var colorRLayerMid : LayerDrawable
    private lateinit var colorRDrawableMid : Drawable

    private lateinit var seekbar: MySeekbar
    //private lateinit var isLinearView : Button
    private lateinit var imageButton: ImageButton

    //lateinit var tileimageview: MyImageView

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
        savedInstanceState: Bundle?): View? {

        _fragmentColorBinding = FragmentColorBinding.inflate(inflater, container, false)

        colorRLayerRight =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_right, null) as LayerDrawable
        colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeDrawable

        colorRLayerMid =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_mid, null) as LayerDrawable
        colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeDrawable

        colorRLayerLeft =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_left, null) as LayerDrawable
        colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeDrawable

        colorRangeRight = binding.palleft
        colorRangeRight?.setForeground(colorRLayerRight)
        setColorRangeRightBackground()

        colorRangeMid = binding.palmid
        colorRangeMid?.setForeground(colorRLayerMid)
        setColorRangeMidBackground()

        colorRangeLeft = binding.palright
        colorRangeLeft?.setForeground(colorRLayerLeft)
        setColorRangeLeftBackground()

        colorsRow = ColorsRow(resources, binding)

        seekbar = binding.seekBar

        imageButton = binding.paletteScaler

        //isLinearView = binding.dataColourConstraint.dataToColourType


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*
        seekbar = view.findViewById(R.id.seekBar)

        imageButton = view.findViewById(R.id.palette_scaler)

        isLinearView = view.findViewById(R.id.data_to_colour_type)
*/

/*

        val palmid = view.findViewById<Button>(R.id.palmid)

        val layer : LayerDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_right, null) as LayerDrawable

        val drawable : Drawable =
            MainActivity.colorClass.getPaletteByID(1).colorRangeBitmap.toDrawable(resources)

        layer.setDrawableByLayerId(R.id.layer_image, drawable)

        palmid.setBackground(layer)
*/

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

            } )

        colorsRow.colorButtons[0]?.button?.setOnClickListener {

        }
        colorsRow.colorButtons[1]?.button?.setOnClickListener {

        }
        colorsRow.colorButtons[2]?.button?.setOnClickListener {

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

        view.findViewById<Button>(R.id.palleft).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.selectPrevColorRange()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }

        view.findViewById<Button>(R.id.palright).setOnClickListener {
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

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }

        view.findViewById<Button>(R.id.add_new_palette2).setOnClickListener {
            if (!calcActive) {
                MainActivity.colorClass.addNewColorB()

                seekbar.progress = MainActivity.colorClass.getProgress()
                seekbar.secondaryProgress = seekbar.progress

                setAnalysisButtonTitle()

                updateTextures()

                //layer.setDrawableByLayerId(R.id.layer_image, MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources))

                //palmid.setBackground(layer)
            }
        }

    }



    private fun setAllColorRangeBackgrounds(){
        setColorRangeRightBackground()
        setColorRangeMidBackground()
        setColorRangeLeftBackground()
    }

    private fun setColorRangeRightBackground(){
        colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerRight.setDrawableByLayerId(R.id.layer_image_r, colorRDrawableRight)
        colorRangeRight?.invalidate()
    }
    private fun setColorRangeMidBackground(){
        colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerMid.setDrawableByLayerId(R.id.layer_image_m, colorRDrawableMid)
        colorRangeMid?.invalidate()
    }
    private fun setColorRangeLeftBackground(){
        colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerLeft.setDrawableByLayerId(R.id.layer_image_l, colorRDrawableLeft)
        //colorRangeLeft?.setForeground(colorRLayerLeft)
        colorRangeLeft?.invalidate()
    }

    private fun updateTextures(setTileView: Boolean = true) {
        jobTextures = CoroutineScope(Dispatchers.IO).launch {
            MainActivity.colorClass.aCurrentRange.updateColorSpreadBitmap()

            setAllColorRangeBackgrounds()

            colorsRow.setColors(MainActivity.colorClass.aCurrentRange.primaryColors)

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
        val text : String = if (MainActivity.colorClass.aCurrentRange.dataProcess == MainActivity.Companion.DataProcess.LINEAR) {
            "Linear"
        } else {
            "Statistical"
        }
       //isLinearView.text = text.subSequence(0, text.length)
    }
}