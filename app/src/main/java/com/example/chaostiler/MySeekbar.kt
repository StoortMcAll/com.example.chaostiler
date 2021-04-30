package com.example.chaostiler

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.graphics.drawable.toDrawable
import com.example.chaostiler.Bitmap_ColorSpread.Companion.mNewColors
import com.example.chaostiler.MainActivity.Companion.colorClass
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax
//import com.example.chaostiler.MainActivity.Companion.mSeekbarProgress


class MySeekbar: androidx.appcompat.widget.AppCompatSeekBar {

    // region Variable Declaration

    var isFirstDraw = true
    var isFinalValue = false

    lateinit var bitmapColorSpread : Bitmap_ColorSpread

    var sbTexture : Bitmap = Bitmap.createBitmap(256, 1, Bitmap.Config.ARGB_8888)

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
        max = mSeekbarMax
        progress = colorClass.getCurrentRange().prog

        mSeekBarHintPaint = TextPaint()
        mSeekBarHintPaint!!.color = mHintTextColor
        mSeekBarHintPaint!!.textSize = mHintTextSize

        bitmapColorSpread = Bitmap_ColorSpread()

        this.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                    colorClass.getCurrentRange().prog = progress

                   // mSeekbarProgress = progress
                    isFirstDraw = true
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    colorClass.getCurrentRange().prog = progress
                    isFirstDraw = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    colorClass.getCurrentRange().prog = progress
                    isFirstDraw = true
                    isFinalValue = true
                }
            })

        invalidate()
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        if (mNewColors) {
            isFirstDraw = true
        }

        if (isFirstDraw) {
            drawSpread()
        }

        super.onDraw(canvas)
    }

    private fun drawSpread() {
        mSeekbarMax = this.max

        sbTexture = bitmapColorSpread.drawBitmap(mSeekbarMax, progress)

        val ib = rootView.findViewById<ImageButton>(R.id.palette_scaler)

        if (ib != null) ib.background = sbTexture.toDrawable(resources)

        if (isFinalValue){
            applyPaletteChangeToBitmap()
        }

        isFinalValue = false
        isFirstDraw = false
    }

}