package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.widget.TextView
//import com.example.chaostiler.FirstFragment.mMaxHitsText
import com.example.chaostiler.MainActivity.Companion.colorClass
import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax
import com.example.chaostiler.MainActivity.Companion.width
import kotlinx.coroutines.*

var doingCalc = false

var square = SquareValues(0.23, 0.7157, -0.4212, 1.3134, -2.632, 1.59, 1.205, -1.34)

var bmTexture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(width * height)

lateinit var tileImageView: MyImageView
lateinit var mMaxHitsText: TextView

var job : Job? = null

// endregion


fun blurLeft(){
    MainActivity.scopeIO.launch {
        prepareBlurData()

        setTileViewBitmap(pixelDataClone)
    }
}
fun blurRight(){
    MainActivity.scopeIO.launch {
        prepareBlurData2()

        setTileViewBitmap(pixelDataClone)
    }
}

fun undoAllChanges() {
    MainActivity.scopeIO.launch {
        pixelDataClone = pixelData.Clone()

        setTileViewBitmap(pixelDataClone)
    }
}


fun setToZero() {
    MainActivity.scopeIO.launch {
        runSetToZero()

        setTileViewBitmap(pixelDataClone)
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

//var begin = System.nanoTime()
//var end = System.nanoTime()
fun startNewRunFormula(isNewRun : Boolean) {
    if (isNewRun) {
        square = SquareValues(MainActivity.rand.nextInt(until = 3))

        pixelData.clearData()
        pixelDataClone.clearData()
    }

    maxCounter = maxCount

    doingCalc = true

    do {
        val hits = runSquare(width, height, square)

        pixelData.addHitsToPixelArray(hits)

        aColors = buildPixelArrayFromColorsIncremental(pixelData)

        bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

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

     tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
 }


fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
    if (job == null || job?.isActive == false){
        job = MainActivity.scopeIO.launch {
            setTileViewBitmap(pixeldatacopy)
        }
    }
}

fun setTileViewBitmap(pixeldatacopy: PixelData) {
    aColors = buildPixelArrayFromColorStats(pixeldatacopy)

    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}

fun buildPixelArrayFromColorsIncremental(pixeldata: PixelData) : IntArray {
    val colrange = colorClass.getCurrentRange()

    val colspreadcount = colrange.mColorSpreadCount

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    var cl : Int

    for (i in 0  until count){
        cl = pixeldata.aPixelArray[i]

        if (cl > colspreadcount) cl = colspreadcount

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}

fun buildPixelArrayFromColorStats(pixeldata: PixelData) : IntArray {
    val colrange = colorClass.getCurrentRange()

    val colspreadcount = colrange.mColorSpreadCount
    val colspreadmaxrange = colrange.prog / mSeekbarMax.toFloat()
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

//Standard Spread
fun buildPixelArrayFromColorSpread(pixeldata: PixelData) : IntArray {
    val cols = IntArray(width * height)

    val colrange = colorClass.getCurrentRange()

    val colrangecount = colrange.mColorSpreadCount * BitmapColorSpread.maxRangeValue

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



