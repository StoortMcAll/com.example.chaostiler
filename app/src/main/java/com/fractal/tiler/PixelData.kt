package com.fractal.tiler

// region Variable Declaration

import com.fractal.tiler.MainActivity.Companion.height
import com.fractal.tiler.MainActivity.Companion.width

data class Hit(val x: Int, val y: Int)

var pixelData = PixelData(width, height)
var pixelDataClone = PixelData(width, height)

val Blur = Filter(16.0F, 0, arrayOf(
    intArrayOf(1, 2, 1),
    intArrayOf(2, 4, 2),
    intArrayOf(1, 2, 1)))

val Gaussian = Filter(0, arrayOf(
    intArrayOf(2, 4, 5, 4, 2),
    intArrayOf(4, 9, 12, 9, 4),
    intArrayOf(5, 12, 15, 12, 5),
    intArrayOf(4, 9, 12, 9, 4),
    intArrayOf(2, 4, 5, 4, 2)))

val Motion = Filter(9.0F, 0, arrayOf(
    intArrayOf(1, 0, 0, 0, 1),
    intArrayOf(0, 1, 0, 1, 0),
    intArrayOf(0, 0, 1, 0, 0),
    intArrayOf(0, 1, 0, 1, 0),
    intArrayOf(1, 0, 0, 0, 1)))

val BoxBlur = Filter(25.0F, 0, arrayOf(
    intArrayOf(1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 1)))

val Median = Filter(-1.0F, 0, arrayOf(
    intArrayOf(1, 2, 1),
    intArrayOf(2, 4, 2),
    intArrayOf(1, 2, 1)))

// endregion


class PixelData(val width : Int, val height : Int) {

    // region Variable Declaration

    val arraySize = width * height

    var mPixelArrayBusy = false

    var mMinHits : Int = 0
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

            if (index < 0) {
                mPixelArrayBusy = false
                return false
            }

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

        while (aHitStats[mMinHits] == 0) mMinHits++

        mPixelArrayBusy = false

        if (mMaxHits / mHitsCount.toDouble() > 0.01)
            return false

        return true
    }

    fun recalcHitStats() {
        mHitsCount = 0
        mMinHits = 0
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

        if(mMaxHits == 0){
            mMaxHits++
            aHitStats.add(0)
        }
        else{
            while (aHitStats[mMinHits] == 0) mMinHits++
        }
    }


    fun clearData() {
        mMinHits = 0
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





class Filter(val kernel : Array<IntArray>) {

    // region Variable Declaration

    var weight = 1

    private var offset = 0.0F

    val kernWid : Int = kernel[0].size

    val kernHit : Int = kernel.size

    val offBot : Int = kernHit / 2
    val offTop : Int = offBot
    val offRight : Int = kernWid / 2
    val offLeft : Int = offRight

    // endregion

    constructor(weight : Float, offset : Int, kernel : Array<IntArray> ) : this(kernel){
        this.weight = weight.toInt()

        this.offset = offset.toFloat()
    }

    constructor(offset : Int, kernel : Array<IntArray> ) : this(kernel){
        for (ky in 0 until kernHit){
            for (kx in 0 until kernWid){
                weight += kernel[ky][kx]
            }
        }

        if (weight < 0) weight = 1

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

    var hits : Int

    var middlehit : Int

    var i = 0

    if (weight == -1) {
        val kernsize = kernWid * kernHit

        val hitlist = mutableListOf<Int>()
        val midindex = kernsize / 2
        var index : Int
        var ik : Int
        for (y in 0 until width) {
            for (x in 0 until height) {
                hitlist.clear()
                index = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        ik = 0
                        hits = wideArray[y + ky][x + kx] * kernel[ky][kx]
                        while(ik < index && hits > hitlist[ik]) ik++
                        hitlist.add(ik, hits)
                        index++
                    }
                }

                middlehit = wideArray[y + offBot][x + offRight]

                array[i++] = middlehit + ((hitlist[midindex] - middlehit) * 0.75F).toInt()

                //array[i++] = hitlist[midindex]
            }
        }
    }
    else {
        val wt = 1.0F / weight
        for (y in 0 until width) {
            for (x in 0 until height) {
                hits = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        hits += wideArray[y + ky][x + kx] * kernel[ky][kx]
                    }
                }

/*

                middlehit = wideArray[y + offBot][x + offRight]

                array[i++] = middlehit + ((hits * wt - middlehit) * 0.5F).toInt()
*/

                array[i++] = (hits * wt).toInt()
            }
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