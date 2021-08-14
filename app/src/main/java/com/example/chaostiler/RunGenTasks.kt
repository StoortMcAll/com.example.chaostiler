package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import com.example.chaostiler.FirstFragment.Companion.mMaxHitsText
import com.example.chaostiler.FirstFragment.Companion.tileImageView
import com.example.chaostiler.MainActivity.Companion.DataProcess
import com.example.chaostiler.MainActivity.Companion.bitmapColorSpread
import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax
import com.example.chaostiler.MainActivity.Companion.quiltType
import com.example.chaostiler.MainActivity.Companion.width
import kotlinx.coroutines.*

var doingCalc = false

var square = SquareValues(0.1, 0.334, 0.2, 0.1, -0.9, -0.59, 0.05, -0.34)

var hexagon = HexValues(SquareValues(0.1, 0.3, -0.1,  -0.076, 0.0, -0.59, 0.0, 0.0), 0)

var icon = IconValues(SquareValues(0.1, -0.1, 0.3,  0.65, 0.43, 0.4, 0.0, 0.09), 24)

var bmTexture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(width * height)

var job : Job? = null

// endregion



fun switchProcessType() {
    val currentRange = bitmapColorSpread.aCurrentRange
    if (currentRange.dataProcess == DataProcess.LINEAR){
        currentRange.dataProcess = DataProcess.STATISTICAL
    }
    else{
        currentRange.dataProcess = DataProcess.LINEAR
    }

}


fun runSetToZero() {
    val arraySize = pixelDataClone.arraySize

    var min = 0

    while (pixelDataClone.mPixelArrayBusy) {
    }

    while (pixelDataClone.aHitStats[min] == 0){
        min++
    }

    if (min > 0) {
        for (i in 0 until arraySize) {
            pixelDataClone.aPixelArray[i] -= min
        }

        pixelDataClone.mMaxHits -= min
        pixelDataClone.mHitsCount -= min * arraySize

        while (pixelDataClone.aHitStats[0] == 0){
            pixelDataClone.aHitStats.removeFirst()
        }
    }
}


fun startNewRunFormula(isNewRun : Boolean) {
    if (isNewRun) {
        prepareForNewRun()
    }

    maxCounter = maxCount

    doingCalc = true

    do {
        val hits : ArrayList<Hit>

        hits = when (quiltType) {
            MainActivity.Companion.QuiltType.SQUARE -> runSquare(width, height, square)
            MainActivity.Companion.QuiltType.HEXAGONAL -> runHexagon(width, height, hexagon)
            else -> runIcon(width, height, icon)
        }

        if (pixelData.addHitsToPixelArray(hits)) {
            aColors = if (bitmapColorSpread.aCurrentRange.dataProcess == DataProcess.LINEAR) {
                buildPixelArrayFromIncrementalColors(pixelData)
            } else {
                buildPixelArrayFromStatisticalColors(pixelData)
            }
        }
        else{
            prepareForNewRun()
        }
        CoroutineScope(Dispatchers.Main).launch {
            upDataUI()
        }

    } while (doingCalc)
}

fun prepareForNewRun(){
    square = SquareValues(SquareValues(0.1, 0.334, 0.2, 0.1, -0.9, -0.59, 0.05, -0.34),
        MainActivity.rand.nextDouble(0.5, 4.0))

    val hex = SquareValues(0.1, 0.3, -0.1, -0.76, 0.0, -0.59, 0.0, 0.0, 0.0, 0.1)
    hexagon = HexValues(hex, MainActivity.rand.nextInt(until = 2))

    icon = initIcon()
    randomizeIconValues()

    pixelData.clearData()
}

fun upDataUI() {
    val value = pixelData.mMaxHits.toString()
    val iters = pixelData.mHitsCount.toString()

    val text = FirstFragment.mHits + " " + value + " " + FirstFragment.mTotal + " " + iters
    mMaxHitsText.text = text.subSequence(0, text.length)

    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}


fun setTileViewBitmap(pixeldatacopy: PixelData) {
    aColors = when (bitmapColorSpread.aCurrentRange.dataProcess) {
        DataProcess.LINEAR -> {
            buildPixelArrayFromIncrementalColors(pixeldatacopy)
        }
        else -> {
            buildPixelArrayFromStatisticalColors(pixeldatacopy)
        }
    }
    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    CoroutineScope(Dispatchers.Main).launch {
        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
    }
}

fun buildPixelArrayFromIncrementalColors(pixeldata: PixelData) : IntArray {
    val curRange = bitmapColorSpread.aCurrentRange

    val mColors = curRange.aColorSpread

    val seekPosAsFraction = curRange.getRangeProgress() * (1.0 / mSeekbarMax.toDouble())

    val leastcolors = 32
    val colorscount = leastcolors + ((curRange.mColorSpreadCount - leastcolors) * seekPosAsFraction).toInt()

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    var mult = 1.0F
    if (pixeldata.mMaxHits == 0){
        cols[0] = mColors[0]
    } else{
        mult /= pixeldata.mMaxHits
    }
    var cl : Int

    for (i in 0  until count){
        cl = ((pixeldata.aPixelArray[i] * mult) * colorscount).toInt()

        if (cl > colorscount) cl = colorscount

        cols[i] = mColors[cl]
    }

    return  cols
}

fun buildPixelArrayFromStatisticalColors(pixeldata: PixelData) : IntArray {
    val colrange = bitmapColorSpread.aCurrentRange

    val colspreadcount = colrange.mColorSpreadCount

    val colspreadmaxrange = colrange.getRangeProgress() * (1.0 / mSeekbarMax.toFloat())

    val count = pixeldata.arraySize
    val cols = IntArray(count)

    var percentage = calculateHitDistribution(pixeldata)
    percentage = flattenDistribution(0.75 + (0.25 * colspreadmaxrange), percentage)

    var basecol : Float
    var statcol : Int
    var df : Int
    for (i in 1..pixeldata.mMaxHits){
        basecol = (i / pixeldata.mMaxHits.toFloat()) * colspreadcount

        statcol = (percentage[i] * colspreadcount).toInt()

        df = (statcol - basecol).toInt()

        basecol += (df * colspreadmaxrange).toInt()

        percentage[i] = basecol
    }

    var cl : Int

    for (i in 0  until count){
        cl = pixeldata.aPixelArray[i]

        cl = percentage[cl].toInt()

        if (cl > colspreadcount) cl = colspreadcount

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}

private fun calculateHitDistribution(pixeldata: PixelData) : FloatArray{
    val percentage = FloatArray(pixeldata.mMaxHits + 1){0.0F}
    for (i in 1 until pixeldata.mMaxHits){
        percentage[i] = percentage[i - 1] + (pixeldata.aHitStats[i - 1] / pixeldata.arraySize.toFloat())
    }
    percentage[pixeldata.mMaxHits] = 1.0F

    var n = 0

    while (pixeldata.aHitStats[n] == 0) { n++ }

    if (n > 1){
        val df = percentage[n] / n
        for (i in 1 until n){
            percentage[i] = i * df
        }
    }

    return percentage
}

private fun flattenDistribution(value : Double, percentage : FloatArray) : FloatArray{
    var n = 0

    while (n < percentage.lastIndex && percentage[n] == 0.0F) n++

    if (n == percentage.lastIndex) return percentage

    if (value > 1.0) return percentage

    val valueoverone = (1.0 / value).toFloat()

    for (i in 0..percentage.lastIndex){
        percentage[i] *= valueoverone
        if (percentage[i] > 1.0) percentage[i] = 1.0F
    }

    return percentage
}


