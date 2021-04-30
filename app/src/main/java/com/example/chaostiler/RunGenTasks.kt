package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.chaostiler.GenerateFragment.Companion.genView
import com.example.chaostiler.MainActivity.Companion.colorClass
import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

var doingCalc = false

var square = SquareValues(0.23, 0.7157, -0.4212, 1.3134, -2.632, 1.59, 1.205, -1.34)

var bmTexture = Bitmap.createBitmap(MainActivity.width, MainActivity.height, Bitmap.Config.ARGB_8888)

var aColors = IntArray(MainActivity.width * MainActivity.height)

lateinit var tileImageView: MyImageView

lateinit var maxHitsView : TextView

// endregion


fun applyPaletteChangeToBitmap(){
    coroutineScope.launch(Dispatchers.Default) {
        setTileViewBitnap()
    }
}

fun setToZero() {
    coroutineScope.launch(Dispatchers.Default) {
        runSetToZero()
    }
}

fun runSetToZero(){
    coroutineScope.launch(Dispatchers.Default) {
        val size = width * height

        var min = 9999

        while (pixelData.mPixelArrayBusy) {
        }

        for (i in 0 until size) {
            if (pixelData.aPixelArray[i] < min) {
                min = pixelData.aPixelArray[i]
            }

            if (min > 0) {

                for (i in 0 until size) {
                    pixelData.aPixelArray[i] = pixelData.aPixelArray[i] - min
                }
            }
        }

        pixelData.mMaxHits -= min

        aColors = buildPixelArrayFromColorSpread(pixelData)

        bmTexture.setPixels(aColors,
            0,
            MainActivity.width,
            0,
            0,
            MainActivity.width,
            MainActivity.height)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

    }
}


fun startNew_RunFormula() {
    square = SquareValues(MainActivity.rand.nextInt(until = 3))

    pixelData.clearData()

    maxCount = 5000

    doingCalc = true

    coroutineScope.launch(Dispatchers.Default) {
        runFormula()
    }
}


fun resume_RunFormula() {
    maxCount = 5000

    doingCalc = true

    coroutineScope.launch(Dispatchers.Default) {
        runFormula()
    }
}

fun runFormula() {
    coroutineScope.launch(Dispatchers.Default) {
      //  GenerateFragment.genView.addView(MainActivity.tv_dynamic)

        do {
            val hits = runSquare(MainActivity.width, MainActivity.height, square)

            pixelData.addHitsToPixelArray(hits)

            val value = pixelData.mMaxHits.toString()

            val text = "Max Hits  $value"

            maxHitsView.text = text

            aColors = buildPixelArrayFromColorSpread(pixelData)

            bmTexture.setPixels(aColors,
                0,
                MainActivity.width,
                0,
                0,
                MainActivity.width,
                MainActivity.height)

            tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
        } while (doingCalc)

   //     genView.removeView(MainActivity.tv_dynamic)
    }
}


fun setTileViewBitnap() {
    aColors = buildPixelArrayFromColorSpread(pixelData)

    bmTexture.setPixels(aColors,
        0,
        MainActivity.width,
        0,
        0,
        MainActivity.width,
        MainActivity.height)

    tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
}

fun buildPixelArrayFromColorSpread(pixeldata: PixelData) : IntArray {
    val colrange = colorClass.getCurrentRange()

    val colrangecount = colrange.mColorSpreadCount * Bitmap_ColorSpread.maxRangeValue

    val mult = colrangecount  / pixeldata.mMaxHits.toDouble()
    val cols = IntArray(MainActivity.width * MainActivity.height)
    val count = pixeldata.arraySize
    var cl : Int

    for (i in 0  until count){
        cl = (pixeldata.aPixelArray[i] * mult).toInt()
       // cl = pixeldata.aPixelArray[i]

        if (cl > colrangecount) cl = colrangecount.toInt()

        cols[i] = colrange.aColorSpread[cl]
    }

    return  cols
}



