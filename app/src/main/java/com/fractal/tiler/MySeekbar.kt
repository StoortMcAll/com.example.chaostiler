package com.fractal.tiler

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.mColorRangeLastIndex


class MySeekbar: androidx.appcompat.widget.AppCompatSeekBar {

    // region Variable Declaration

    private var mSeekBarHintPaint: Paint? = null
    private var mHintTextColor: Int = 0
    private var mHintTextSize: Float = 0.toFloat()

    // endregion


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SeekbarHint,
            0, 0
        )
        try {
            mHintTextColor = a.getColor(R.styleable.SeekbarHint_hint_text_color, 0)
            mHintTextSize = a.getDimension(R.styleable.SeekbarHint_hint_text_size, 0f)
        } finally {
            a.recycle()
        }

        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        max = mColorRangeLastIndex
        if (MainActivity.colorClass.aCurrentRange.dataProcess == DataProcess.LINEAR) {
            progress = MainActivity.colorClass.aCurrentRange.progressIncrement
        }
        else{
            progress = MainActivity.colorClass.aCurrentRange.progressStatistic
        }
        secondaryProgress = 0//MainActivity.colorClass.aCurrentRange.progressSecond

        mSeekBarHintPaint = TextPaint()
        mSeekBarHintPaint!!.color = mHintTextColor
        mSeekBarHintPaint!!.textSize = mHintTextSize

        invalidate()
    }

}