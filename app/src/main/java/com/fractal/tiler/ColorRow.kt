package com.fractal.tiler

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.fractal.tiler.databinding.FragmentColorBinding


class ColorImageButton(val button: ImageButton?, drawableLayerId : Int,
                       private val itemInLayerId: Int, val buttonId : Int, private val hasStateList : Boolean = false){

    private var layerBackground : LayerDrawable
    private var backgroundDrawable : Drawable
    private var backgroundColor = 0xff7f7f7f.toInt()

    private val layerForeground : LayerDrawable?

    var isActive = false

    init {
        layerBackground = ResourcesCompat.getDrawable(
            MainActivity.myResources, drawableLayerId, null) as LayerDrawable

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

    fun setButtonBackground(drawable: Drawable){
        backgroundDrawable = drawable

        layerBackground.setDrawableByLayerId(itemInLayerId, backgroundDrawable)

        button?.invalidate()
    }

    fun setButtonForeground(){

    }
}


class ColorsRow(binding : FragmentColorBinding, focusedButtonId : Int) {

    // region Variable Declaration

    private var lastIndex = 4

    private val buttonStatesDrawable = ResourcesCompat.getDrawable(MainActivity.myResources,
        R.drawable.color_button_states,
        null) as StateListDrawable

    private val buttonPressedDrawable = ResourcesCompat.getDrawable(MainActivity.myResources,
        R.drawable.layer_button_color_pressed,
        null) as LayerDrawable

    val colorButtons: Array<ColorImageButton>

    var activeButtonId = 0

    // endregion


    init {
        colorButtons = arrayOf(
            ColorImageButton(binding.startColor,
                R.drawable.layer_background_color,
                R.id.layer_color,
                0),
            ColorImageButton(binding.color0,
                R.drawable.layer_background_color,
                R.id.layer_color,
                1,
                true),
            ColorImageButton(binding.color1,
                R.drawable.layer_background_color,
                R.id.layer_color,
                2,
                true),
            ColorImageButton(binding.color2,
                R.drawable.layer_background_color,
                R.id.layer_color,
                3,
                true),
            ColorImageButton(binding.endColor,
                R.drawable.layer_background_color,
                R.id.layer_color,
                4))

        setActiveButton(focusedButtonId)
    }

    fun setColors(primaryColors: IntArray, activeButtonId: Int) {
        lastIndex = primaryColors.lastIndex
        this.activeButtonId = activeButtonId

        colorButtons[0].setButtonBackground(primaryColors[0].toDrawable())
        colorButtons[4].setButtonBackground(primaryColors[lastIndex].toDrawable())

        for (i in 1..3) {
            if (lastIndex > i) {
                colorButtons[i].setButtonBackground(primaryColors[i].toDrawable())
                colorButtons[i].button?.isVisible = true
            } else {
                colorButtons[i].setButtonBackground(Color.GRAY.toDrawable())
                colorButtons[i].button?.isVisible = false
            }

            if (activeButtonId == i) {
                colorButtons[activeButtonId].button?.requestFocus()
                colorButtons[i].isActive = true
            } else {
                colorButtons[i].isActive = false
            }
        }
    }

    fun setActiveButton(buttonId: Int) {
        if (buttonId != activeButtonId) {
            colorButtons[activeButtonId].isActive = false
        }

        activeButtonId = buttonId

        colorButtons[activeButtonId].isActive = true
        //colorButtons[activeButtonId].button?.requestFocus()
    }

}
