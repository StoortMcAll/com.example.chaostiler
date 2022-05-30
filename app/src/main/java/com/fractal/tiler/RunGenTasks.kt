package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.TextView
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.colorClass
import com.fractal.tiler.MainActivity.Companion.focusLost
import com.fractal.tiler.MainActivity.Companion.height
import com.fractal.tiler.MainActivity.Companion.mColorRangeLastIndex
import com.fractal.tiler.MainActivity.Companion.quiltType
import com.fractal.tiler.MainActivity.Companion.width
import kotlinx.coroutines.*
import kotlin.math.*

var doingCalc = false

var square = SquareValues(0.1, 0.334, 0.2, 0.1, -0.9, -0.59, 0.05, -0.34)

var hexagon = HexValues(SquareValues(0.1, 0.3, -0.1,  -0.076, 0.0, -0.59, 0.0, 0.0), 0)

var icon = IconValues(SquareValues(0.1, -0.1, 0.3,  0.65, 0.43, 0.4, 0.0, 0.09), 24)

var bmTexture : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(width * height)

var job : Job? = null


lateinit var imageView : MyImageView
lateinit var hitsInfoView : TextView

// endregion

fun setTileImageView(imageview : MyImageView){
    imageView = imageview
}
fun setHitsInfoTextView(hitsview : TextView){
    hitsInfoView = hitsview
}

/*

fun switchProcessType() {
    val currentRange = MainActivity.colorClass.aCurrentRange
    if (currentRange.dataProcess == DataProcess.LINEAR){
        currentRange.dataProcess = DataProcess.STATISTICAL
    }
    else{
        currentRange.dataProcess = DataProcess.LINEAR
    }

}
*/


fun runSetToZero() {
    val arraySize = pixelDataClone.arraySize

    var min = 0

    while (pixelDataClone.mPixelArrayBusy) { }

    while (pixelDataClone.aHitStats[min] == 0) min++

    if (min > 0) {
        for (i in 0 until arraySize) {
            pixelDataClone.aHitsArray[i] -= min
        }

        pixelDataClone.mMaxHits -= min
        pixelDataClone.mHitsCount -= min * arraySize

        while (pixelDataClone.aHitStats[0] == 0){
            pixelDataClone.aHitStats.removeFirst()
        }
    }

    if (pixelDataClone.mMaxHits < 50) return

    var index = 0
    var total = 0.0F

    val hitstats = pixelDataClone.aHitStats

    do{
        total += hitstats[index]
        index++

    } while ((total / arraySize) < 0.9995f)
    index--

    if (index == pixelDataClone.mMaxHits) return

    pixelDataClone.mMaxHits = index
    pixelDataClone.mHitsCount = 0
    pixelDataClone.aHitStats = mutableListOf<Int>()

    for (i in 0..index) pixelDataClone.aHitStats.add(0)

    var hits : Int
    for (i in 0 until arraySize) {
        hits = pixelDataClone.aHitsArray[i]
        if (hits > index){
            hits = index
            pixelDataClone.aHitsArray[i] = hits
        }

        pixelDataClone.mHitsCount += hits
        pixelDataClone.aHitStats[hits]++
    }
}


fun startNewRunFormula(isNewRun : Boolean) {
    if (isNewRun) {
        prepareForNewRun()
    }

    maxCounter = maxCount

    focusLost = false

    doingCalc = true

    do {
        val hits = when (quiltType) {
            MainActivity.Companion.QuiltType.SQUARE -> runSquare(width, height, square)
            MainActivity.Companion.QuiltType.HEXAGONAL -> runHexagon(width, height, hexagon)
            else -> runIcon(width, height, icon)
        }

        if (pixelData.addHitsToPixelArray(hits)) {
            if (MainActivity.colorRangeChangeAnimInProgess) {
                while (colorClass.mBlendingColors){ }

                aColors = buildPixelArrayFromAnimColors(pixelData, MainActivity.animColorSpread.copyOf())
            }
            else
                aColors =buildPixelArrayFromIncrementalColors(pixelData)
        } else{
            prepareForNewRun()
        }

        bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

        CoroutineScope(Dispatchers.Main).launch {
            upDataUI()
        }

        if (focusLost) doingCalc = false

    } while (doingCalc)
}

fun prepareForNewRun(){
    square = SquareValues(SquareValues(0.1, 0.334, 0.2, 0.1, -0.9, -0.59, 0.05, -0.34),
        MainActivity.rand.nextDouble(0.5, 4.0))

    val hex = SquareValues(0.1, 0.3, -0.1, -0.076, 0.0, -0.59, 0.0, 0.0, 0.0, 0.1)
    hexagon = HexValues(hex, MainActivity.rand.nextInt(until = 2))

    icon = initIcon()
    randomizeIconValues()

    pixelData.clearData()
}

fun upDataUI() {
    val mmin = pixelData.mMinHits.toString()
    val mmax = pixelData.mMaxHits.toString()

    val text = FirstFragment.mHitsMinString+ " "  + mmin.padStart(4)+ " "  + FirstFragment.mHitsMaxString + " " + mmax.padStart(4)
    hitsInfoView.text = text.subSequence(0, text.length)

    imageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}


fun setTileViewBitmap(pixeldatacopy: PixelData, useTanScale : Boolean = false) {

    if (useTanScale)
        aColors = buildPixelArrayColorsFromTanScale(pixeldatacopy)
    else
        aColors = buildPixelArrayFromIncrementalColors(pixeldatacopy)

    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    CoroutineScope(Dispatchers.Main).launch {
        imageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
    }
}


fun buildPixelArrayFromIncrementalColors(pixeldata: PixelData) : IntArray {
    val curRange = MainActivity.colorClass.aCurrentRange

    val mColors = curRange.aColorSpread

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    val aScaleMaxHit = IntArray(pixeldata.mMaxHits + 1)

    val scalar = mColorRangeLastIndex / pixeldata.mMaxHits.toFloat()
    for (i in 0..pixeldata.mMaxHits)
        aScaleMaxHit[i] = (i * scalar).toInt()

    for (i in 0  until count){
        cols[i] = mColors[aScaleMaxHit[pixeldata.aHitsArray[i]]]
    }

    return  cols
}

fun buildPixelArrayFromAnimColors(pixeldata: PixelData, mColors : IntArray) : IntArray {
    val curRange = MainActivity.colorClass.aCurrentRange

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    val aScaleMaxHit = IntArray(pixeldata.mMaxHits + 1)

    val scalar = mColorRangeLastIndex / pixeldata.mMaxHits.toFloat()
    for (i in 0..pixeldata.mMaxHits)
        aScaleMaxHit[i] = (i * scalar).toInt()

    for (i in 0  until count){
        cols[i] = mColors[aScaleMaxHit[pixeldata.aHitsArray[i]]]
    }

    return  cols
}

fun buildPixelArrayColorsFromTanScale(pixeldata: PixelData) : IntArray {
    val curRange = MainActivity.colorClass.aCurrentRange

    val mColors = curRange.aColorSpread

    val count = pixeldata.arraySize

    val cols = IntArray(count)

    for (i in 0  until count){
        cols[i] = mColors[pixeldata.aTanIndex[pixeldata.aHitsArray[i]]]
    }

    return  cols
}


