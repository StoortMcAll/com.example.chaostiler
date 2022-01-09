package com.fractal.tiler

// region Variable Declaration

import com.fractal.tiler.MainActivity.Companion.height
import com.fractal.tiler.MainActivity.Companion.width
import kotlin.math.atan
import kotlin.math.tan

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

val Sharpen = Filter(-8.0F, 0, arrayOf(
    intArrayOf(1, 1, 1),
    intArrayOf(1, 0, 1),
    intArrayOf(1, 1, 1)))

val Smooth = Filter(-9.0F, 0, arrayOf(
    intArrayOf(1, 1, 1),
    intArrayOf(1, 1, 1),
    intArrayOf(1, 1, 1)))

// endregion

class PixelData(val width : Int, val height : Int) {

    // region Variable Declaration

    val arraySize = width * height

    var mPixelArrayBusy = false

    var mMinHits = 0
    var mMaxHits = 0

    var mHitsCount = 0

    var aHitStats : MutableList<Int> = mutableListOf(arraySize)
    var aTanIndex : MutableList<Int> = mutableListOf(arraySize)

    val aHitsArray = IntArray(arraySize)

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

            value = aHitsArray[index]
            aHitsArray[index]++

            aHitStats[value]--
            value++

            if (value > mMaxHits) {
                mMaxHits++
                aHitStats.add(0)
               // aTanIndex.add(0)
            }

            aHitStats[value]++
        }

        while (aHitStats[mMinHits] == 0) mMinHits++

        mPixelArrayBusy = false

        if (mMaxHits / mHitsCount.toDouble() > 0.01)
            return false

        return true
    }

    fun recalcScaledHitStats() {
        return

        mHitsCount = 0
        mMinHits = 0
        mMaxHits = 0
        aHitStats = arrayListOf(0)
        aTanIndex = arrayListOf(0)

        var hits : Int
        for (i in 0..aHitsArray.lastIndex){
            hits = aHitsArray[i]

            mHitsCount += hits

            while(hits > mMaxHits){
                mMaxHits++
                aHitStats.add(0)
                aTanIndex.add(mMaxHits)
            }

            aHitStats[hits]++
        }

        while (aHitStats[mMinHits] == 0) mMinHits++
    }

    fun calcTangentScale() {
        val maxAngle = 0.4 + (1.15 * MainActivity.dataFragmentSeekbarProgress)
        val mult = tan(maxAngle) / mMaxHits
        val colmult = MainActivity.mColorRangeLastIndex / maxAngle
        var r : Int

        for (i in 0..mMaxHits) {
            r = (atan(i * mult) * colmult).toInt()

            aTanIndex[i] = r
        }
    }

    fun clearData() {
        mMinHits = 0
        mMaxHits = 0

        mHitsCount = 0

        aHitsArray.fill(0, 0, aHitsArray.count())

        aHitStats = mutableListOf(arraySize)

        aTanIndex = mutableListOf(0)
    }

    fun clone(): PixelData {
        val clonePD = PixelData(width, height)

        clonePD.mMinHits = mMinHits

        reduceMax(clonePD)

        return clonePD
    }

    fun reduceMax(clonePD : PixelData){
        val arry = aHitsArray
        val newStats = IntArray(aHitStats.size)

        var count : Int
        var i = 0
        var iref = 0
        while (i < aHitStats.size){
            count = aHitStats[i]
            newStats[i] = iref

            if (count < 10) {
                while (count < 10) {
                    if (++i == aHitStats.size) {
                        count = 10
                    } else {
                        count += aHitStats[i]
                        newStats[i] = iref
                    }

                    iref++
                }
            }else {
                i++
                iref++
            }
        }

        clonePD.mMaxHits = newStats[newStats.lastIndex]

        clonePD.aHitStats = mutableListOf()
        clonePD.aTanIndex = mutableListOf()

        val maxh = newStats[newStats.lastIndex]
        val finalStats = IntArray(maxh + 1)
        count = 0
        var n = 0

        for (i in 0..arry.lastIndex){
            n = newStats[arry[i]]
            clonePD.aHitsArray[i] = n
            finalStats[n]++
        }

        for (i in 0..finalStats.lastIndex) {
            n = finalStats[i]
            count += n * i
            clonePD.aHitStats.add(n)
            clonePD.aTanIndex.add(i)
        }

        clonePD.mHitsCount = count
    }
}
