package com.example.chaostiler

// region Variable Declaration

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width

data class Hit(val x: Int, val y: Int)

var pixelData = PixelData(width, height)
var pixelDataClone = PixelData(width, height)

// endregion


class PixelData(private val width : Int, private val height : Int) {

    // region Variable Declaration

    val arraySize = width * height

    var mPixelArrayBusy = false

    var mMaxHits : Int = 0

    var mHitsCount = 0

    var aHitStats : MutableList<Int> = mutableListOf(arraySize)

    var aPixelArray : IntArray = IntArray(arraySize)

    // endregion


    fun addHitsToPixelArray(hits : ArrayList<Hit>) {
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
    }

    fun clearData()
    {
        mMaxHits = 0

        mHitsCount = 0

        aPixelArray.fill(0, 0, aPixelArray.count())

        aHitStats = mutableListOf(arraySize)
    }

    fun Clone(): PixelData {
        var clonePD = PixelData(width, height)

        clonePD.mMaxHits = mMaxHits

        clonePD.mHitsCount = mHitsCount

        clonePD.aPixelArray = aPixelArray.clone()

        clonePD.aHitStats = mutableListOf()
        for (i in 0 until aHitStats.size) {
            clonePD.aHitStats.add(aHitStats[i])
        }

        return clonePD
    }
}

