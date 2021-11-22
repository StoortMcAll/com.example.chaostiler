package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.widget.TextView
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.height
import com.fractal.tiler.MainActivity.Companion.mSeekbarMax
import com.fractal.tiler.MainActivity.Companion.quiltType
import com.fractal.tiler.MainActivity.Companion.width
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

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

fun switchProcessType() {
    val currentRange = MainActivity.colorClass.aCurrentRange
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

    while (pixelDataClone.mPixelArrayBusy) { }

    while (pixelDataClone.aHitStats[min] == 0) min++

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
        hits = pixelDataClone.aPixelArray[i]
        if (hits > index){
            hits = index
            pixelDataClone.aPixelArray[i] = hits
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

    doingCalc = true

    do {
        val hits = when (quiltType) {
            MainActivity.Companion.QuiltType.SQUARE -> runSquare(width, height, square)
            MainActivity.Companion.QuiltType.HEXAGONAL -> runHexagon(width, height, hexagon)
            else -> runIcon(width, height, icon)
        }

        if (pixelData.addHitsToPixelArray(hits)) {
            aColors = if (MainActivity.colorClass.aCurrentRange.dataProcess == DataProcess.LINEAR) {
                buildPixelArrayFromIncrementalColors(pixelData)
            } else {
                buildPixelArrayFromSinwave(pixelData)
            }
        }
        else{
            prepareForNewRun()
        }

        bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

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
    val mmin = pixelData.mMinHits.toString()
    val mmax = pixelData.mMaxHits.toString()

    val text = FirstFragment.mHitsMinString+ " "  + mmin.padStart(4)+ " "  + FirstFragment.mHitsMaxString + " " + mmax.padStart(4)
    hitsInfoView.text = text.subSequence(0, text.length)


    imageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
    //tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}


fun setTileViewBitmap(pixeldatacopy: PixelData) {
    aColors = when (MainActivity.colorClass.aCurrentRange.dataProcess) {
        DataProcess.LINEAR -> {
            buildPixelArrayFromIncrementalColors(pixeldatacopy)
        }
        else -> {
            buildPixelArrayFromSinwave(pixeldatacopy)
        }
    }
    bmTexture.setPixels(aColors, 0, width, 0, 0, width, height)

    CoroutineScope(Dispatchers.Main).launch {
        imageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
        //tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
    }
}


fun buildPixelArrayFromIncrementalColors(pixeldata: PixelData) : IntArray {
    val curRange = MainActivity.colorClass.aCurrentRange

    val mColors = curRange.aColorSpread

    val seekPosAsFraction = curRange.getRangeProgress() * (1.0 / mSeekbarMax.toDouble())

    val leastcolors = 32
    val colorscount = leastcolors + ((mColors.lastIndex - leastcolors) * seekPosAsFraction).toInt()

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


fun buildPixelArrayFromSinwave(pixeldata: PixelData) : IntArray {
    val colrange = MainActivity.colorClass.aCurrentRange

    val colspreadcount = colrange.aColorSpread.lastIndex

    val seekPosAsFraction = colrange.getRangeProgress() * (1.0 / mSeekbarMax.toFloat())

    val count = pixeldata.arraySize
    val cols = IntArray(count)
    val hitindex = IntArray(pixeldata.mMaxHits + 1)

    val arc = PI / 2.0
    val wd = pixeldata.mMaxHits
    val widthover1 = 1.0f / wd
    var valueInc : Float
    var valueArc : Float
    var dif : Float
    var index : Int

    for (x in 0..wd) {
        valueInc = x * widthover1
        valueArc = sin(valueInc * arc).toFloat()

        dif = valueArc - valueInc

        index = ((valueInc + dif * seekPosAsFraction) * colspreadcount).toInt()

        hitindex[x] = index
    }

    for (i in 0  until count){
        index = pixeldata.aPixelArray[i]

        cols[i] = colrange.aColorSpread[hitindex[index]]
    }

    return  cols
}



fun buildPixelArrayFromCosecColors(pixeldata: PixelData) : IntArray {
    val colrange = MainActivity.colorClass.aCurrentRange

    val colspreadcount = colrange.aColorSpread.lastIndex

    val colspreadmaxrange = colrange.getRangeProgress() * (1.0 / mSeekbarMax.toFloat())

    val count = pixeldata.arraySize
    val cols = IntArray(count)

    val adjustedIndex = setCosecValues(pixeldata.mMaxHits, colspreadcount)

    val finalIndex = IntArray(pixeldata.mMaxHits + 1){0}

    var basecol : Float
    var statcol : Int
    var df : Int
    for (i in 1..pixeldata.mMaxHits){
        basecol = (i / pixeldata.mMaxHits.toFloat()) * colspreadcount

        statcol = adjustedIndex[i]

        df = (statcol - basecol).toInt()

        basecol += (df * colspreadmaxrange).toInt()

        finalIndex[i] = basecol.toInt()
    }

    var cl : Int

    for (i in 0  until count){
        cl = pixeldata.aPixelArray[i]

        cl = finalIndex[cl]

        if (cl > colspreadcount) cl = colspreadcount

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}

private fun setCosecValues(maxHit : Int, colorsCount : Int) : IntArray{
    val max = acos(1.0 / (1.0 + 1.0))

    val slice = if (maxHit == 0) { 0.0 } else { max / maxHit  }

    val mult = colorsCount / max

    val colorIndex = IntArray(maxHit + 1)

    for (x in 0..maxHit){
        colorIndex[x]= (mult * ((1.0 / cos(x * slice)) - 1.0)).toInt()
    }

    return colorIndex
}


fun buildPixelArrayFromStatisticalColors(pixeldata: PixelData) : IntArray {
    val colrange = MainActivity.colorClass.aCurrentRange

    val colspreadcount = colrange.aColorSpread.lastIndex

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


