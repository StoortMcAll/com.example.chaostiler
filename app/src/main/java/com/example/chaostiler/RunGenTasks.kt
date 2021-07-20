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

var square = SquareValues(0.23, 0.7157, -0.4212, 1.3134, -2.632, 1.59, 1.205, -1.34)

var hexagon = HexValues()

var bmTexture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(width * height)

var job : Job? = null

// endregion


fun blurLeft() {
    prepareBlurData()
}
fun blurRight(){
    prepareBlurData2()
}


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
        square = SquareValues(MainActivity.rand.nextInt(until = 3))

        hexagon = HexValues(MainActivity.rand.nextInt(until = 3))

        pixelData.clearData()
        //pixelDataClone.clearData()
    }

    maxCounter = maxCount

    doingCalc = true

    do {
        val hits : ArrayList<Hit>

        if (quiltType == MainActivity.Companion.QuiltType.Square)
            hits = runSquare(width, height, square)
        else
            hits = runHexagon(width, height, hexagon)

        pixelData.addHitsToPixelArray(hits)

        if (bitmapColorSpread.aCurrentRange.dataProcess == DataProcess.LINEAR){
            aColors = buildPixelArrayFromIncrementalColors(pixelData)
        }
        else{
            aColors = buildPixelArrayFromStatisticalColors(pixelData)
        }

        CoroutineScope(Dispatchers.Main).launch {
            upDataUI()
        }

    } while (doingCalc)
}

fun upDataUI() {
    val value = pixelData.mMaxHits.toString()
    val iters = pixelData.mHitsCount.toString()

    val text = "Hits : Max - $value   Total - $iters "
    mMaxHitsText.text = text.subSequence(0, text.length)

    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}


fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData) : Boolean{
    if (job == null || job?.isActive == false){
        job = MainActivity.scopeIO.launch {
            setTileViewBitmap(pixeldatacopy)
        }
        return true
    } else {
        job?.cancel()

        return false
    }
}

fun setTileViewBitmap(pixeldatacopy: PixelData) {
    if (bitmapColorSpread.aCurrentRange.dataProcess == DataProcess.LINEAR) {
        aColors = buildPixelArrayFromIncrementalColors(pixeldatacopy)
    } else {
        aColors = buildPixelArrayFromStatisticalColors(pixeldatacopy)
    }
    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    CoroutineScope(Dispatchers.Main).launch {


        tileImageView.setBitmap(bmTexture)//.copy(Bitmap.Config.ARGB_8888, false))
    }
}

fun buildPixelArrayFromIncrementalColors(pixeldata: PixelData) : IntArray {
    val curRange = bitmapColorSpread.aCurrentRange

    val mColors = curRange.aColorSpread

    val colspreadcount = curRange.mColorSpreadCount

    val seekPosAsFraction = curRange.getRangeProgress() * (1.0 / mSeekbarMax.toDouble())

    val colorscount = 32 + ((colspreadcount - 32) * seekPosAsFraction).toInt()

    //val colorscount = (0.125 + 0.875 * seekPosAsFraction).toFloat()

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    val maxhitsover1 = 1.0F / pixeldata.mMaxHits.toFloat()

    var cl : Int

    for (i in 0  until count){
        cl = ((pixeldata.aPixelArray[i] * maxhitsover1) * colorscount).toInt()
        //cl = (pixeldata.aPixelArray[i] * colorscount).toInt()

        if (cl > colspreadcount) cl = colspreadcount

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

    val percentage = FloatArray(pixeldata.mMaxHits + 1){0.0F}

    for (i in 1 until pixeldata.mMaxHits){
        percentage[i] = percentage[i - 1] + (pixeldata.aHitStats[i - 1] / count.toFloat())
    }
    percentage[pixeldata.mMaxHits] = 1.0F

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



