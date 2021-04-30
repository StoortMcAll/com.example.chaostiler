package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import com.example.chaostiler.MainActivity.Companion.colorClass

//endregion


class Bitmap_ColorSpread {

    // region Variable Declaration

    companion object {
        private var colArray = IntArray(256)

        var mNewColors = false

        var maxRangeValue = 1.0
    }

    // endregion

    fun setMaxRangeValue(maxPos : Int, currentPos : Int)
    {
        maxRangeValue = 0.25 + 0.75 * (currentPos *  (1.0 / maxPos.toDouble()))
    }

    fun drawBitmap(maxPos : Int, currentPos : Int) : Bitmap {
        var bitmap : Bitmap = Bitmap.createBitmap(256, 1, Bitmap.Config.ARGB_8888)

        val curRange = colorClass.getCurrentRange()

        val mColors = curRange.aColorSpread

        val colorscount = curRange.mColorSpreadCount

        maxRangeValue = 0.25 + 0.75 * (currentPos *  (1.0 / maxPos.toDouble()))

        val bmWid = bitmap.width
        val wd = bmWid - 1
        val dx = 1.0f / wd
        var value: Int

        for (x in 0..wd) {
            value = (colorscount * (maxRangeValue * (x * dx))).toInt()

            colArray[x] = mColors[value]
        }

        bitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        mNewColors = true

        return bitmap
    }

}