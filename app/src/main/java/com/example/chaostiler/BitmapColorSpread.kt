package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import com.example.chaostiler.MainActivity.Companion.colorClass
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax

//endregion


class BitmapColorSpread {

    // region Variable Declaration

    companion object {
        private var colArray = IntArray(mSeekbarMax)

        var mNewColors = false

        var maxRangeValue = 1.0
    }

    // endregion


    fun drawBitmap(maxPos : Int, currentPos : Int, pixelDataCopy : PixelData) : Bitmap {
        val bitmap : Bitmap = Bitmap.createBitmap(mSeekbarMax, 1, Bitmap.Config.ARGB_8888)

        val curRange = colorClass.getCurrentRange()

        val mColors = curRange.aColorSpread

        val maxhits = pixelDataCopy.mMaxHits

        maxRangeValue = currentPos * (1.0 / maxPos.toDouble())

        val colorscount : Int
        if (maxhits > curRange.mColorSpreadCount) {
            colorscount = curRange.mColorSpreadCount

        } else{
            colorscount = maxhits + ((curRange.mColorSpreadCount - maxhits) * maxRangeValue).toInt()
        }


        val bmWid = bitmap.width
        val wd = bmWid - 1
        val widthover1 = 1.0f / wd
        var value: Int

        for (x in 0..wd) {
            value = (colorscount * (x * widthover1)).toInt()

            colArray[x] = mColors[value]
        }

        bitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        mNewColors = true

        return bitmap
    }

    fun drawBitmap2(maxPos : Int, currentPos : Int, pixelDataCopy : PixelData) : Bitmap {
        val bitmap : Bitmap = Bitmap.createBitmap(mSeekbarMax, 1, Bitmap.Config.ARGB_8888)

        val curRange = colorClass.getCurrentRange()

        val mColors = curRange.aColorSpread

        val colorscount = curRange.mColorSpreadCount

        val maxhits = pixelDataCopy.mMaxHits

        maxRangeValue = currentPos * (1.0 / maxPos.toDouble())

        val percentage = FloatArray(pixelDataCopy.mMaxHits + 1){0.0F}
        for (i in 1 until pixelDataCopy.mMaxHits){
            percentage[i] = percentage[i - 1] + (pixelDataCopy.aHitStats[i - 1] / pixelDataCopy.arraySize.toFloat())
        }
        percentage[pixelDataCopy.mMaxHits] = 1.0F

        val bmWid = bitmap.width
        val wd = bmWid - 1
        val dx = 1.0f / wd
        var value: Int
        var findex : Float
        var basecol : Int
        var df : Int
        var fpos : Float

        for (x in 0 until wd) {
            fpos = maxhits * (x * dx)

            basecol = ((x * dx) * colorscount).toInt()

            value = fpos.toInt()

            findex = percentage[value]

            fpos -= value

            findex += (percentage[value + 1] - percentage[value]) * fpos

            value = (colorscount * findex).toInt()

            df = value - basecol

            basecol += (df * maxRangeValue).toInt()

            colArray[x] = mColors[basecol]
        }
        colArray[wd] = mColors[colorscount]

        bitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        mNewColors = true

        return bitmap
    }
}