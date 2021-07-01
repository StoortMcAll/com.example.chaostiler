package com.example.chaostiler

// region Variable Declaration

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width

data class Hit(val x: Int, val y: Int)

var pixelData = PixelData(width, height)

// endregion


class PixelData(private val width : Int, private val height : Int) {

    // region Variable Declaration

    val arraySize = width * height

    var mPixelArrayBusy = false

    var mMaxHits : Int = 0

    var aPixelArray : IntArray = IntArray(arraySize)

    // endregion


    fun addHitsToPixelArray(hits : ArrayList<Hit>) {
        var index : Int

        mPixelArrayBusy = true

        hits.forEach {
            index = it.x + it.y * width
            aPixelArray[index]++

            if (aPixelArray[index] > mMaxHits)
                mMaxHits = aPixelArray[index]
        }

        mPixelArrayBusy = false
    }

    fun clearData()
    {
        mMaxHits = 0

        aPixelArray.fill(0, 0, aPixelArray.count() - 1)
    }

    fun Clone(): PixelData {
        var clonePD = PixelData(width, height)

        clonePD.mMaxHits = mMaxHits

        clonePD.aPixelArray = aPixelArray.clone()

        return clonePD
    }
}

