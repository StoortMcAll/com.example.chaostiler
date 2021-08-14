package com.example.chaostiler

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.graphics.drawable.toDrawable
import com.example.chaostiler.MainActivity.Companion.DataProcess
import com.example.chaostiler.MainActivity.Companion.bitmapColorSpread
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax


class MySeekbar: androidx.appcompat.widget.AppCompatSeekBar {

    // region Variable Declaration
/*

    var isFirstDraw = true
    var isFinalValue = false
*/

    val bitmapColorSpread = MainActivity.bitmapColorSpread

   // var sbTexture : Bitmap = Bitmap.createBitmap(mSeekbarMax, 1, Bitmap.Config.ARGB_8888)

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
        if (bitmapColorSpread.aCurrentRange.dataProcess == DataProcess.LINEAR) {
            progress = bitmapColorSpread.aCurrentRange.progressIncrement
        }
        else{
            progress = bitmapColorSpread.aCurrentRange.progressStatistic
        }
        secondaryProgress = bitmapColorSpread.aCurrentRange.progressSecond

        mSeekBarHintPaint = TextPaint()
        mSeekBarHintPaint!!.color = mHintTextColor
        mSeekBarHintPaint!!.textSize = mHintTextSize

/*

        this.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    currentColorClass.aCurrentRange.progressIncrement = progress
                    isFirstDraw = true
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    currentColorClass.aCurrentRange.progressIncrement = seekBar.progress
                    isFirstDraw = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    seekBar.secondaryProgress = seekBar.progress
                    currentColorClass.aCurrentRange.progressIncrement = seekBar.progress
                    currentColorClass.aCurrentRange.progressSecond = seekBar.secondaryProgress
                    isFirstDraw = true
                    isFinalValue = true
                }
            })
*/

        invalidate()
    }

   /* override fun performClick(): Boolean {
        return super.performClick()
    }
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
            }
        }

        invalidate()

        return true
    }

*/

    /* @Synchronized
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
         //bitmapColorSpread.updateColorSpreadBitmap(mSeekbarMax, progress, pixelDataClone)
 
         sbTexture = bitmapColorSpread.seekbarBitmap
 
         val ib = rootView.findViewById<ImageButton>(R.id.palette_scaler)
 
         if (ib != null) ib.background = sbTexture.toDrawable(resources)
 
         //if (isFinalValue){
             applyPaletteChangeToBitmap(pixelDataClone)
         //}
 
         isFinalValue = false
         isFirstDraw = false
     }*/

}