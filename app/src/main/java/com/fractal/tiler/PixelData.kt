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

    var aHitsArray : IntArray = IntArray(arraySize)

    var aScaledHitsArray : IntArray = IntArray(arraySize)

    // endregion


    fun addHitsToPixelArray(hits : ArrayList<Hit>) : Boolean{
        var index : Int
        var value : Int

        var newMaxHits = false

        mPixelArrayBusy = true

        mHitsCount += hits.size

        hits.forEach {
            index = it.x + it.y * width

            if (index < 0) {
                mPixelArrayBusy = false
                return false
            }

            value = aHitsArray[index]
            aHitsArray[index]++

            aHitStats[value]--
            value++

            if (value > mMaxHits) {
                newMaxHits = true
                mMaxHits++
                aHitStats.add(0)
            }

            aHitStats[value]++
        }

        while (aHitStats[mMinHits] == 0) mMinHits++

        val scalar =
            if (mMaxHits == 0) 0.0F
            else MainActivity.mColorRangeLastIndex / mMaxHits.toFloat()

        if (newMaxHits){
            val scaledValues = IntArray(mMaxHits + 1)

            for (i in 0..mMaxHits){
                scaledValues[i] = (i * scalar).toInt()
            }

            for (i in 0 until arraySize){
                aScaledHitsArray[i] = scaledValues[aHitsArray[i]]
            }
        }else {
            hits.forEach {
                index = it.x + it.y * width
                aScaledHitsArray[index] = (aHitsArray[index] * scalar).toInt()
            }
        }

        mPixelArrayBusy = false

        if (mMaxHits / mHitsCount.toDouble() > 0.01)
            return false

        return true
    }

    fun recalcScaledHitsArray(newMax : Int){
        val colorRangeLastIndex = MainActivity.mColorRangeLastIndex
        val scalar =
            if (newMax == 0) 0.0F
            else colorRangeLastIndex / newMax.toFloat()

        val scaledValues = IntArray(mMaxHits + 1)

        for (i in 0..mMaxHits){
            if (i > newMax)
                scaledValues[i] = colorRangeLastIndex
            else
                scaledValues[i] = (i * scalar).toInt()
        }

        for (i in 0 until arraySize){
            aScaledHitsArray[i] = scaledValues[aHitsArray[i]]
        }
    }

    fun recalcHitStats() {
        mHitsCount = 0
        mMinHits = 0
        mMaxHits = 0
        aHitStats = arrayListOf(0)

        var hits : Int
        for (i in 0..aHitsArray.lastIndex){
            hits = aHitsArray[i]

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

        aHitsArray.fill(0, 0, aHitsArray.count())
        aScaledHitsArray.fill(0, 0, aScaledHitsArray.count())

        aHitStats = mutableListOf(arraySize)
    }

    fun clone(): PixelData {
        val clonePD = PixelData(width, height)

        clonePD.mMaxHits = mMaxHits

        clonePD.mHitsCount = mHitsCount

        clonePD.aHitsArray = aHitsArray.clone()
        clonePD.aScaledHitsArray = aScaledHitsArray.clone()

        clonePD.aHitStats = mutableListOf()
        for (i in 0..aHitStats.lastIndex) {
            clonePD.aHitStats.add(aHitStats[i])
        }

        return clonePD
    }

}
