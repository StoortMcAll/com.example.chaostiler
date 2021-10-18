package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.mSeekbarMax
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

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
        aCurrentRange = colorClass.addNewRandomHSVRange()

        mNewColors = true
    }
    fun addNewColorB(){
        aCurrentRange = colorClass.addNewRandomPrimariesRange()

        mNewColors = true
    }


    fun updateColorSpreadBitmap(pixelDataCopy : PixelData){
        seekbarBitmap = if (aCurrentRange.dataProcess == DataProcess.LINEAR) {
            drawColorSpreadForIncremental(aCurrentRange.progressIncrement)
        } else{
            drawColorSpreadForSinwave(aCurrentRange.progressStatistic, pixelDataCopy)
        }

        mNewColors = true
    }


    private fun drawColorSpreadForIncremental(currentPos : Int) : Bitmap {

        val curRange = aCurrentRange

        val mColors = curRange.aColorSpread

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

        val colorscount = 32 + ((mColors.lastIndex - 32) * seekPosAsFraction).toInt()

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

    private fun drawColorSpreadForSinwave(currentPos : Int, pixelDataCopy : PixelData) : Bitmap {

        val mColors = aCurrentRange.aColorSpread

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

        val colorscount = mColors.lastIndex

        val arc = PI / 2.0
        val bmWid = seekbarBitmap.width
        val wd = bmWid - 1
        val widthover1 = 1.0f / wd
        var valueInc : Float
        var valueArc : Float
        var dif : Float
        var index : Int

        for (x in 0..wd) {
            valueInc = x * widthover1
            valueArc = sin(valueInc * arc).toFloat()

            dif = valueArc - valueInc

            index = ((valueInc + dif * seekPosAsFraction) * colorscount).toInt()

            colArray[x] = mColors[index]
        }

        seekbarBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)

        mNewColors = true

        return seekbarBitmap
    }


    private fun drawColorSpreadForStatistical(currentPos : Int, pixelDataCopy : PixelData) : Bitmap {
        val curRange = aCurrentRange

        val mColors = curRange.aColorSpread

        val colorscount = mColors.lastIndex

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

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

    private fun drawColorSpreadForCosec(currentPos : Int, pixelDataCopy : PixelData) : Bitmap {
        val curRange = aCurrentRange

        val mColors = curRange.aColorSpread

        val colorscount = mColors.lastIndex

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

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

    private fun setCosecValues(width : Int, colorsCount : Int) : IntArray{
        val max = acos(1.0 / (width + 1.0))

        val slice = max / width

        val mult = colorsCount / width.toDouble()

        val colorIndex = IntArray(width + 1)

        for (x in 0..width){
            colorIndex[x]= (mult * ((1.0 / cos(x * slice)) - 1.0)).toInt()
        }

        return colorIndex
    }
}