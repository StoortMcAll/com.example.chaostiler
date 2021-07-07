package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.util.Log
import android.widget.TextView
import com.example.chaostiler.FirstFragment.Companion.mMaxHitsText
import com.example.chaostiler.MainActivity.Companion.colorClass
import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var doingCalc = false
var jobRunning = false

var square = SquareValues(0.23, 0.7157, -0.4212, 1.3134, -2.632, 1.59, 1.205, -1.34)

var bmTexture = Bitmap.createBitmap(MainActivity.width, MainActivity.height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(MainActivity.width * MainActivity.height)

lateinit var tileImageView: MyImageView

// endregion


fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
    coroutineScope.launch(Dispatchers.Default) {
        setTileViewBitnap(pixeldatacopy)
    }
}

fun blurLeft(){
    coroutineScope.launch(Dispatchers.Default) {
        prepareBlurData()

        setTileViewBitnap(pixelDataClone)
    }
}

fun setToZero() {
    coroutineScope.launch(Dispatchers.Default) {
        runSetToZero()

        setTileViewBitnap(pixelDataClone)
    }
}

fun runSetToZero() {
    val size = width * height

    var min = 9999

    while (pixelDataClone.mPixelArrayBusy) {
    }

    for (i in 0 until size) {
        if (pixelDataClone.aPixelArray[i] < min) {
            min = pixelDataClone.aPixelArray[i]
        }
    }

    if (min > 0) {
        for (i in 0 until size) {
            pixelDataClone.aPixelArray[i] = pixelDataClone.aPixelArray[i] - min
        }

        pixelDataClone.mMaxHits -= min
    }
}


fun startNew_RunFormula(isNewRun : Boolean, runCounter : Int) {
    if (isNewRun) {
        square = SquareValues(MainActivity.rand.nextInt(until = 3))

        pixelData.clearData()
        pixelDataClone.clearData()
    }
    maxCounter = maxCount

    doingCalc = true

    Log.d("RunCount", runCounter.toString())
    do {
        val hits = runSquare(MainActivity.width, MainActivity.height, square)

        pixelData.addHitsToPixelArray(hits)

        aColors = buildPixelArrayFromColorSpread(pixelData)

        bmTexture.setPixels(aColors,
            0,
            width,
            0,
            0,
            width,
            height)

        CoroutineScope(Dispatchers.Main).launch {
            upDataUI()
        }

    } while (doingCalc)
    Log.d("RunCountEnd", runCounter.toString())
}

 fun upDataUI(){

        val value = pixelData.mMaxHits.toString()

        var text = "Max Hits  $value"

        mMaxHitsText.text = text.subSequence(0, text.length)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))


}

fun setTileViewBitnap(pixeldatacopy: PixelData) {
    aColors = buildPixelArrayFromColorSpread(pixeldatacopy)

    bmTexture.setPixels(aColors,
        0,
        MainActivity.width,
        0,
        0,
        MainActivity.width,
        MainActivity.height)

    tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}

fun buildPixelArrayFromColorSpreadBlur(pixeldata: PixelData) : IntArray {
    val colrange = colorClass.getCurrentRange()

    val colrangecount = colrange.mColorSpreadCount * Bitmap_ColorSpread.maxRangeValue

    val mult = colrangecount  / pixeldata.mMaxHits.toDouble()
    val cols = IntArray(MainActivity.width * MainActivity.height)
    val count = pixeldata.arraySize
    var cl : Int

    for (i in 0  until count){
        cl = (pixeldata.aPixelArray[i] * mult).toInt()
        // cl = pixeldata.aPixelArray[i]

        if (cl > colrangecount) cl = colrangecount.toInt()

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}

fun buildPixelArrayFromColorSpread(pixeldata: PixelData) : IntArray {
    val cols = IntArray(MainActivity.width * MainActivity.height)

    val colrange = colorClass.getCurrentRange()

    val colrangecount = colrange.mColorSpreadCount * Bitmap_ColorSpread.maxRangeValue

    val mult = colrangecount  / pixeldata.mMaxHits.toDouble()

    val count = pixeldata.arraySize
    var cl : Int
    val maxval = colrangecount.toInt()

    for (i in 0  until count){
        cl = (pixeldata.aPixelArray[i] * mult).toInt()

        if (cl > maxval) cl = maxval

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}



