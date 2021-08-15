package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import com.fractal.tiler.MainActivity.Companion.mSeekbarMax

//endregion


class BitmapColorSpread {

    // region Variable Declaration
    var seekbarBitmap : Bitmap = Bitmap.createBitmap(mSeekbarMax, 1, Bitmap.Config.ARGB_8888)

    var mNewColors = true

    val colorClass = ColorClass()

    var aCurrentRange = colorClass.aCurrentRange

    private var colArray = IntArray(mSeekbarMax)

    // endregion

    fun getProgress() : Int{
        return aCurrentRange.getRangeProgress()
    }

    fun setProgress(prog : Int){
        aCurrentRange.setRangeProgress(prog)
    }

    fun nextPalette(){
        aCurrentRange = colorClass.increaseSpreadID()

        mNewColors = true
    }

    fun prevPalette(){
        aCurrentRange = colorClass.decreaseSpreadID()

        mNewColors = true
    }

    fun addNewColorA(){
        aCurrentRange = colorClass.addNewRandomPrimariesRange()

        mNewColors = true
    }
    fun addNewColorB(){
        aCurrentRange = colorClass.addNewRandomColorsRange()

        mNewColors = true
    }

    fun updateColorSpreadBitmap(pixelDataCopy : PixelData){
        if (aCurrentRange.dataProcess == MainActivity.Companion.DataProcess.LINEAR) {
            seekbarBitmap = drawColorSpreadForIncremental(mSeekbarMax, aCurrentRange.progressIncrement)
        }
        else{
            seekbarBitmap = drawColorSpreadForStatistical(mSeekbarMax, aCurrentRange.progressStatistic, pixelDataCopy)
        }

        mNewColors = true
    }


    private fun drawColorSpreadForIncremental(maxPos : Int, currentPos : Int) : Bitmap {

        val curRange = aCurrentRange

        val mColors = curRange.aColorSpread

        val seekPosAsFraction = currentPos * (1.0 / maxPos.toDouble())

        val colorscount = 32 + ((curRange.mColorSpreadCount - 32) * seekPosAsFraction).toInt()

        val bmWid = seekbarBitmap.width
        val wd = bmWid - 1
        val widthover1 = 1.0f / wd
        var value: Int

        for (x in 0..wd) {
            value = (colorscount * (x * widthover1)).toInt()

            colArray[x] = mColors[value]
        }

        seekbarBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        mNewColors = true

        return seekbarBitmap
    }

    private fun drawColorSpreadForStatistical(maxPos : Int, currentPos : Int, pixelDataCopy : PixelData) : Bitmap {
        val curRange = aCurrentRange

        val mColors = curRange.aColorSpread

        val colorscount = curRange.mColorSpreadCount

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / maxPos.toDouble())

        val percentage = FloatArray(pixelDataCopy.mMaxHits + 1){0.0F}
        for (i in 1 until pixelDataCopy.mMaxHits){
            percentage[i] = percentage[i - 1] + (pixelDataCopy.aHitStats[i - 1] / pixelDataCopy.arraySize.toFloat())
        }
        percentage[pixelDataCopy.mMaxHits] = 1.0F

        val bmWid = seekbarBitmap.width
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

            basecol += (df * seekPosAsFraction).toInt()

            colArray[x] = mColors[basecol]
        }
        colArray[wd] = mColors[colorscount]

        seekbarBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        return seekbarBitmap
    }
}