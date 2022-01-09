package com.fractal.tiler

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.*
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.fractal.tiler.databinding.FragmentColorBinding


class ColorImageButton(
    val button: ImageButton?, val buttonId : Int, drawableId: Int,
    private var backgroundColor: Int, var isVisible: Boolean, hasStateList: Boolean = false){

    private var shapeDrawable = GradientDrawable()
    private val layerBackground : LayerDrawable
    private val layerForeground : LayerDrawable?

    var isActive = false
    var layoutParams = button?.layoutParams as ConstraintLayout.LayoutParams

    init {
        layoutParams.matchConstraintPercentWidth = if (isVisible) 0.15f else 0.0f
        button?.layoutParams = layoutParams

        shapeDrawable.cornerRadius = MainActivity.dpToPx
        shapeDrawable.setColor(backgroundColor)

        layerBackground = ResourcesCompat.getDrawable(MainActivity.myResources, R.drawable.layer_rectangle_stroke, null) as LayerDrawable
        layerBackground.setDrawableByLayerId(R.id.layer_rectangle, shapeDrawable)

        button?.setBackground(layerBackground)

        layerForeground = if (hasStateList){
            val foregroundStateList = button?.getForeground() as StateListDrawable
            val drawableContainerState = foregroundStateList.constantState as DrawableContainer.DrawableContainerState

            drawableContainerState.children[0] as LayerDrawable
        } else { null }

    }

    fun setButtonBackground(newColor: Int, setVisibility : Boolean, activeButtonId : Int) : Boolean{
        val isWeightChanged : Boolean
        if (isVisible != setVisibility) {
            isVisible = setVisibility

            layoutParams = button?.layoutParams as ConstraintLayout.LayoutParams

            isWeightChanged = true
        } else isWeightChanged = false

        if (buttonId != activeButtonId) {
            isActive = false
            if (button?.isActivated() == true) button?.isActivated = false
        } else {
            isActive = true
            if (button?.isActivated() == false) button?.isActivated = true
        }

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), backgroundColor, newColor)
        backgroundColor = newColor

        colorAnimation.addUpdateListener { animator : ValueAnimator ->
            shapeDrawable.setColor(animator.animatedValue as Int)
            layerBackground.setDrawableByLayerId(R.id.layer_rectangle, shapeDrawable)

            ViewCompat.setBackground(button as ImageView, layerBackground)
        }
        colorAnimation.duration = 250
        colorAnimation.start()

        return isWeightChanged
    }

}

data class ButtonData(val isVisible : Boolean, val colorIndex : Int)
data class ButtonGroup(val group : Array<ButtonData>){
    fun findButtonIndex(colorIndex : Int) : Int{
        for (i in 0..group.lastIndex){
            if (group[i].colorIndex == colorIndex && group[i].isVisible)
                return i
        }
        return 1
    }
}

class ColorsRow(binding : FragmentColorBinding, buttonColors : IntArray) {

    // region Variable Declaration

    private var lastIndex = buttonColors.lastIndex

    val colorButtons: Array<ColorImageButton>

    private val buttonSets = arrayOf(
        ButtonGroup(arrayOf(
            ButtonData(true, 0),
            ButtonData(false, 1),
            ButtonData(true, 1),
            ButtonData(false, 2),
            ButtonData(true, 2))),
        ButtonGroup(arrayOf(
            ButtonData(true, 0),
            ButtonData(true, 1),
            ButtonData(false, 2),
            ButtonData(true, 2),
            ButtonData(true, 3))),
        ButtonGroup(arrayOf(
            ButtonData(true, 0),
            ButtonData(true, 1),
            ButtonData(true, 2),
            ButtonData(true, 3),
            ButtonData(true, 4)))

    )

    var buttonGroup = buttonSets[lastIndex - 2]

    var activeButtonId = 0

    // endregion


    init {
        colorButtons = arrayOf(
            ColorImageButton(
                binding.startColor, 0,
                R.drawable.layer_rectangle_stroke,
                buttonColors[buttonGroup.group[0].colorIndex],
                buttonGroup.group[0].isVisible),
            ColorImageButton(
                binding.color0,1,
                R.drawable.shape_rectangle,
                buttonColors[buttonGroup.group[1].colorIndex],
                buttonGroup.group[1].isVisible,
                true),
            ColorImageButton(
                binding.color1,2,
                R.drawable.shape_rectangle,
                buttonColors[buttonGroup.group[2].colorIndex],
                buttonGroup.group[2].isVisible,
                true),
            ColorImageButton(
                binding.color2,3,
                R.drawable.shape_rectangle,
                buttonColors[buttonGroup.group[3].colorIndex],
                buttonGroup.group[3].isVisible,
                true),
            ColorImageButton(
                binding.endColor,4,
                R.drawable.shape_rectangle,
                buttonColors[buttonGroup.group[4].colorIndex],
                buttonGroup.group[4].isVisible))

    }

    fun setColors(primaryColors: IntArray, activeColorIndex: Int) {
        lastIndex = primaryColors.lastIndex


        buttonGroup = buttonSets[lastIndex - 2]

        activeButtonId = buttonGroup.findButtonIndex(activeColorIndex)

        var isWeightAnimNeeded = false
        val buttonWeightChanged = BooleanArray(5)
        for (i in 0..4){
            buttonWeightChanged[i] = colorButtons[i].setButtonBackground(
                primaryColors[buttonGroup.group[i].colorIndex],
                buttonGroup.group[i].isVisible,
                activeButtonId)
            if(buttonWeightChanged[i] == true)
                isWeightAnimNeeded = true
        }

        if (isWeightAnimNeeded) {
            val weightAnimation = ValueAnimator.ofFloat(0.0f, 1.0f)

            weightAnimation.duration = 250
            weightAnimation.interpolator = LinearInterpolator()

            var layoutParams : ConstraintLayout.LayoutParams
            weightAnimation.addUpdateListener { animator: ValueAnimator ->
                val animValue = animator.animatedValue as Float
                for (i in 1..3) {
                    if (buttonWeightChanged[i]) {
                        val sign = if (colorButtons[i].isVisible) animValue else 1.0f - animValue
                        layoutParams = colorButtons[i].button?.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.matchConstraintPercentWidth = sign * 0.15f
                        layoutParams.horizontalWeight = sign
                        colorButtons[i].button?.layoutParams = layoutParams
                    }
                }
                colorButtons[0].button?.parent?.requestLayout()
            }

            weightAnimation.start()
        }

    }

    fun setActiveButton(buttonId: Int) : Boolean{
        val buttonChanged : Boolean

        if (buttonId != activeButtonId) {
            buttonChanged = true
            if (activeButtonId > 0) {
                colorButtons[activeButtonId].isActive = false
                if (colorButtons[activeButtonId].button?.isActivated() == true)
                    colorButtons[activeButtonId].button?.isActivated = false
            }
        }
        else buttonChanged = false

        activeButtonId = buttonId

        colorButtons[activeButtonId].isActive = true
        colorButtons[activeButtonId].button?.isActivated = true

        return buttonChanged
    }

}
