package com.example.chaostiler

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width
import kotlin.math.pow
import kotlin.math.sqrt


fun prepareBlurData2(){
    val widewidth = width + 2
    val wideheight = height + 2
    val widehitarray = IntArray(widewidth * wideheight)
    val array = pixelDataClone.aPixelArray

    val posy = width * (height - 1)
    var wposy = widewidth * (wideheight - 1)
    for (x in 0 until width){
        widehitarray[1 + x] = array[posy + x]
        widehitarray[wposy + 1 + x] = array[x]
    }

    var posx = width - 1
    val wposx = widewidth * 2 - 1
    for (y in 0 until height){
        widehitarray[widewidth + y * widewidth] = array[y * width + posx]
        widehitarray[widewidth * y + wposx] = array[y * width]
    }

    var i = 0
    wposy = 1
    for (y in 0 until width){
        wposy += widewidth
        for (x in 0 until height){
            widehitarray[wposy + x] = array[i++]
        }
    }

    var max = pixelDataClone.mMaxHits
    var hits : Double
    var valu : Int
    posx = 0
    for (y in 1 until widewidth - 1){
        wposy = widewidth * y
        for (x in 1 until wideheight - 1){
            hits = widehitarray[wposy + x] * 4.0
            hits += widehitarray[wposy + x - 1]
            hits += widehitarray[wposy + x + 1]
            hits += widehitarray[wposy + x - widewidth]
            hits += widehitarray[wposy + x + widewidth]
            hits += widehitarray[wposy + x - 1- widewidth] * 0.25
            hits += widehitarray[wposy + x + 1 - widewidth] * 0.25
            hits += widehitarray[wposy + x - 1 + widewidth] * 0.25
            hits += widehitarray[wposy + x + 1 + widewidth] * 0.25

            valu = (hits / 9.0).toInt()

            array[posx++] = valu

            if(valu > max) max = valu
        }
    }

    pixelDataClone.mMaxHits = max
}
fun prepareBlurData(){
    val widewidth = width + 2
    val wideheight = height + 2
    val widehitarray = IntArray(widewidth * wideheight)
    val array = pixelDataClone.aPixelArray

    val posy = width * (height - 1)
    var wposy = widewidth * (wideheight - 1)
    for (x in 0 until width){
        widehitarray[1 + x] = array[posy + x]
        widehitarray[wposy + 1 + x] = array[x]
    }

    var posx = width - 1
    val wposx = widewidth * 2 - 1
    for (y in 0 until height){
        widehitarray[widewidth + y * widewidth] = array[y * width + posx]
        widehitarray[widewidth * y + wposx] = array[y * width]
    }

    var i = 0
    wposy = 1
    for (y in 0 until width){
         wposy += widewidth
        for (x in 0 until height){
            widehitarray[wposy + x] = array[i++]
        }

    }

    var max = pixelDataClone.mMaxHits
    var hits : Double
    var valu : Int
    posx = 0
    for (y in 1 until widewidth - 1){
        wposy = widewidth * y
        for (x in 1 until wideheight - 1){
            hits = widehitarray[wposy + x].toDouble().pow(2.0) * 8.0
            hits += widehitarray[wposy + x - 1].toDouble().pow(2.0) * 2.0
            hits += widehitarray[wposy + x + 1].toDouble().pow(2.0) * 2.0
            hits += widehitarray[wposy + x - widewidth].toDouble().pow(2.0) * 2.0
            hits += widehitarray[wposy + x + widewidth].toDouble().pow(2.0) * 2.0
            hits += widehitarray[wposy + x - 1 - widewidth].toDouble().pow(2.0)
            hits += widehitarray[wposy + x + 1 - widewidth].toDouble().pow(2.0)
            hits += widehitarray[wposy + x - 1 + widewidth].toDouble().pow(2.0)
            hits += widehitarray[wposy + x + 1 + widewidth].toDouble().pow(2.0)

            hits /= 20.0

            valu = sqrt(hits).toInt()

            array[posx++] = valu

            if(valu > max) max = valu
        }
    }

    pixelDataClone.mMaxHits = max
}
