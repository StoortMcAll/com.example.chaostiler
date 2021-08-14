package com.example.chaostiler

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width
import kotlin.math.pow
import kotlin.math.sqrt


fun prepareBlurData3(){
    val widewidth = width + 2
    val wideheight = height + 2
    val widehitarray = DoubleArray(widewidth * wideheight)
    val array = pixelDataClone.aPixelArray

    var valu : Double

    var i = 0
    for (y in 1..height){
        var wposy = widewidth * y

        for (x in 1..width){
            valu = array[i++].toDouble()
            widehitarray[x + wposy] += valu

            valu = sqrt(valu)
            widehitarray[x + wposy - 1] += valu
            widehitarray[x + wposy + 1] += valu
            widehitarray[x + wposy - widewidth] += valu
            widehitarray[x + wposy + widewidth] += valu

            valu = sqrt(valu)
            widehitarray[x + wposy - 1 - widewidth] += valu
            widehitarray[x + wposy + 1 - widewidth] += valu
            widehitarray[x + wposy - 1 + widewidth] += valu
            widehitarray[x + wposy + 1 + widewidth] += valu
        }
    }

    var wposy = widewidth * (wideheight - 1)
    for (x in 1..width){
        widehitarray[widewidth + x] += widehitarray[wposy + x]
        widehitarray[wposy - widewidth + x] += widehitarray[x]
    }

    val wposx = widewidth - 1
    for (y in 1..height){
        wposy = widewidth * y
        widehitarray[1 + wposy] += widehitarray[wposy + wposx]
        widehitarray[wposx - 1 + wposy] += widehitarray[wposy]
    }

    i = 0
    for (y in 1..height){
        wposy = widewidth * y
        for (x in 1..width){
            array[i++] = widehitarray[wposy + x].toInt()
        }
    }

    pixelDataClone.recalcHitStats()
}


fun prepareBlurData2(){

    pixelDataClone.recalcHitStats()
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

    var hits : Double
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

            array[posx++] = (hits / 9.0).toInt()
        }
    }
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

    var hits : Double
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

            array[posx++] = sqrt(hits).toInt()
        }
    }

    pixelDataClone.recalcHitStats()
}

