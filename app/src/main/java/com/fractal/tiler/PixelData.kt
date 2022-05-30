package com.fractal.tiler

// region Variable Declaration

import com.fractal.tiler.MainActivity.Companion.height
import com.fractal.tiler.MainActivity.Companion.width
import kotlin.math.atan
import kotlin.math.tan

data class HitCoord(val x: Int, val y: Int)

var pixelData = PixelData(width, height)
var pixelDataClone = PixelData(width, height)

// endregion

class PixelData(val width : Int, val height : Int) {

    // region Variable Declaration

    val arraySize = width * height
    val arrayLastIndex = arraySize - 1

    var mPixelArrayBusy = false

    var mMinHits = 0
    var mMaxHits = 0

    var mHitsCount = 0

    var aHitStats = mutableListOf(arraySize)
    var aTanIndex = mutableListOf(0)

    val aHitsArray = IntArray(arraySize)

    var hitCompressor = HitCompressor(5)

    // endregion

    fun addHitsToPixelArray(hits : ArrayList<HitCoord>) : Boolean{
        var index : Int
        var value : Int

        mPixelArrayBusy = true

        //mHitsCount += hits.size

        hits.forEach {
            index = it.x + it.y * width

            if (index < 0 || index > arrayLastIndex) {
                mPixelArrayBusy = false
                return false
            }

            value = aHitsArray[index]

            if (true) {
                mHitsCount++

                aHitStats[value]--

                value++

                if (value > mMaxHits) {
                    mMaxHits++
                    aHitStats.add(0)
                }

                aHitsArray[index]++

                aHitStats[value]++
            } else {

                if (value  == mMaxHits) {
                    if (hitCompressor.addHitCoord(index)) {
                        //hitCompressor =
                            hitCompressor.pullDataCoordList(this)
                    }
                } else {
                    aHitsArray[index]++

                    aHitStats[value]--
                    value++
                    aHitStats[value]++

                    mHitsCount++
                }
            }
        }

        while (aHitStats[mMinHits] == 0) mMinHits++

        mPixelArrayBusy = false

        if (mMaxHits / mHitsCount.toDouble() > 0.01)
            return false

        return true
    }

    fun recalcScaledHitStats() {
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

        aTanIndex = MutableList(mMaxHits + 1){
            i -> (atan(i * mult) * colmult).toInt()
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
        clonePD.mMaxHits = mMaxHits
        clonePD.mHitsCount = mHitsCount

        clonePD.aHitStats = mutableListOf()
      //  clonePD.aTanIndex = MutableList(mMaxHits + 1){i -> i}

        clonePD.aHitStats.addAll(aHitStats)

        clonePD.calcTangentScale()

        aHitsArray.copyInto(clonePD.aHitsArray, 0, 0, arraySize)

        //reduceMax(clonePD)

        return clonePD
    }

    private fun reduceMax(clonePD : PixelData){
        val arry = aHitsArray
        val newStats = IntArray(aHitStats.size)

        var count : Int
        val countMax = 50

        var i = 0
        var iref = 0
        while (i < aHitStats.size){
            count = aHitStats[i]
            newStats[i] = iref

            if (count < countMax) {
                while (count < countMax) {
                    i++
                    if (i == aHitStats.size) {
                        count = countMax
                    } else {
                        count += aHitStats[i]
                        newStats[i] = iref
                    }
                }
            }else {
                i++
            }
            iref++
        }

        clonePD.mMaxHits = newStats[newStats.lastIndex]

        clonePD.aHitStats = mutableListOf()
        clonePD.aTanIndex = mutableListOf()

       // val maxh = newStats[newStats.lastIndex]
        val finalStats = IntArray(clonePD.mMaxHits + 1)
        count = 0
        var n: Int

        for (j in 0..arry.lastIndex){
            n = newStats[arry[j]]
            clonePD.aHitsArray[j] = n
            finalStats[n]++
        }

        for (k in 0..finalStats.lastIndex) {
            n = finalStats[k]
            count += n * k
            clonePD.aHitStats.add(n)
            clonePD.aTanIndex.add(k)
        }


        clonePD.mMinHits = 0
        while (clonePD.aHitStats[clonePD.mMinHits] == 0) clonePD.mMinHits++

        clonePD.mHitsCount = count
    }
}
