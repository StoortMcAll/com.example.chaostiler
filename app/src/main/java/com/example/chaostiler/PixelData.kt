package com.example.chaostiler

// region Variable Declaration

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width

data class Hit(val x: Int, val y: Int)

var pixelData = PixelData(width, height)
var pixelDataClone = PixelData(width, height)

val Blur = Filter(16.0F, 0, arrayOf(
    floatArrayOf(1.0F, 2.0F, 1.0F),
    floatArrayOf(2.0F, 4.0F, 2.0F),
    floatArrayOf(1.0F, 2.0F, 1.0F)))

val Gaussian = Filter(0, arrayOf(
    floatArrayOf(2.0F, 4.0F, 5.0F, 4.0F, 2.0F),
    floatArrayOf(4.0F, 9.0F, 12.0F, 9.0F, 4.0F),
    floatArrayOf(5.0F, 12.0F, 15.0F, 12.0F, 5.0F),
    floatArrayOf(4.0F, 9.0F, 12.0F, 9.0F, 4.0F),
    floatArrayOf(2.0F, 4.0F, 5.0F, 4.0F, 2.0F)))

val Motion = Filter(9.0F, 0, arrayOf(
    floatArrayOf(1.0F, 0.0F, 0.0F, 0.0F, 1.0F),
    floatArrayOf(0.0F, 1.0F, 0.0F, 1.0F, 0.0F),
    floatArrayOf(0.0F, 0.0F, 1.0F, 0.0F, 0.0F),
    floatArrayOf(0.0F, 1.0F, 0.0F, 1.0F, 0.0F),
    floatArrayOf(1.0F, 0.0F, 0.0F, 0.0F, 1.0F)))

val BoxBlur = Filter(25.0F, 0, arrayOf(
    floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F, 1.0F),
    floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F, 1.0F),
    floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F, 1.0F),
    floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F, 1.0F),
    floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F, 1.0F)))


// endregion


class PixelData(val width : Int, val height : Int) {

    // region Variable Declaration

    val arraySize = width * height

    var mPixelArrayBusy = false

    var mMaxHits : Int = 0

    var mHitsCount = 0

    var aHitStats : MutableList<Int> = mutableListOf(arraySize)

    var aPixelArray : IntArray = IntArray(arraySize)

    // endregion


    fun addHitsToPixelArray(hits : ArrayList<Hit>) : Boolean{
        var index : Int
        var value : Int

        mPixelArrayBusy = true

        mHitsCount += hits.size

        hits.forEach {
            index = it.x + it.y * width

            value = aPixelArray[index]
            aPixelArray[index]++

            aHitStats[value]--
            value++
            if (value > mMaxHits) {
                mMaxHits++
                aHitStats.add(0)
            }

            aHitStats[value]++
        }

        mPixelArrayBusy = false

        if (mMaxHits / mHitsCount.toDouble() > 0.01)
            return false

        return true
    }

    fun recalcHitStats() {
        mHitsCount = 0
        mMaxHits = 0
        aHitStats = arrayListOf(0)

        var hits : Int
        for (i in 0..aPixelArray.lastIndex){
            hits = aPixelArray[i]

            mHitsCount += hits

            while(hits > mMaxHits){
                mMaxHits++
                aHitStats.add(0)
            }

            aHitStats[hits]++
        }
    }


    fun clearData()
    {
        mMaxHits = 0

        mHitsCount = 0

        aPixelArray.fill(0, 0, aPixelArray.count())

        aHitStats = mutableListOf(arraySize)
    }

    fun clone(): PixelData {
        val clonePD = PixelData(width, height)

        clonePD.mMaxHits = mMaxHits

        clonePD.mHitsCount = mHitsCount

        clonePD.aPixelArray = aPixelArray.clone()

        clonePD.aHitStats = mutableListOf()
        for (i in 0..aHitStats.lastIndex) {
            clonePD.aHitStats.add(aHitStats[i])
        }

        return clonePD
    }
}





class Filter(val kernel : Array<FloatArray>) {

    // region Variable Declaration

    var weight = 1.0F

    private var offset = 0.0F

    val kernWid : Int = kernel[0].size

    val kernHit : Int = kernel.size

    val offBot : Int = kernHit / 2
    val offTop : Int = offBot
    val offRight : Int = kernWid / 2
    val offLeft : Int = offRight

    // endregion

    constructor(weight : Float, offset : Int, kernel : Array<FloatArray> ) : this(kernel){
        if (weight < 0)
            this.weight = 1.0F
        else
            this.weight = 1.0F / weight

        this.offset = offset.toFloat()
    }

    constructor(offset : Int, kernel : Array<FloatArray> ) : this(kernel){
        for (ky in 0 until kernHit){
            for (kx in 0 until kernWid){
                weight += kernel[ky][kx]
            }
        }

        if (weight < 0) weight = 1.0F

        weight = 1.0F / weight

        this.offset = offset.toFloat()
    }


    fun doImageFilter(pixelDataCopy : PixelData){
        val wideArray = getWideArray(pixelDataCopy.aPixelArray)

        pixelDataCopy.aPixelArray = performFunction(wideArray)

        pixelDataCopy.recalcHitStats()
    }

}

fun Filter.performFunction(wideArray: Array<IntArray>) : IntArray{

    val array = IntArray(width * height)

    var hits : Float

    var i = 0

    for (y in 0 until width){
        for (x in 0 until height){
            hits = 0.0F
            for (ky in 0 until kernHit){
                for (kx in 0 until kernWid){
                    hits += wideArray[y+ky][x +kx] * kernel[ky][kx]
                }
            }

            hits *= weight
            if (hits < 0) hits = 0.0F

            array[i++] = hits.toInt()
        }
    }

    return array
}

fun Filter.getWideArray(array : IntArray) : Array<IntArray>{
    val widewidth = width + offLeft + offRight
    val wideheight = height + offTop + offBot
    val wideArray = Array(wideheight) { IntArray(widewidth)}

    var i = 0
    var wposy = widewidth * offTop

    for (y in 0 until height){
        for (x in 0 until width){
            wideArray[offBot + y][offLeft + x] = array[i++]
        }
        wposy += widewidth
    }

    i = 0
    for (wh in height until wideheight) {
        for (x in 0 until width) {
            wideArray[wh][offLeft + x] = array[i++]
        }
    }

    for (wh in 0 until offBot) {
        wposy = width * (height - offBot + wh)
        for (x in 0 until width) {
            wideArray[wh][offLeft + x] = array[wposy + x]
        }
    }

    for (y in 0 until wideheight) {
        for (ww in 0 until offLeft) {
            wideArray[y][ww] = wideArray[y][width + ww]
            wideArray[y][offLeft + width + ww] = wideArray[y][offLeft + ww]
        }
    }

    return wideArray
}