package com.example.chaostiler

import com.example.chaostiler.MainActivity.Companion.height
import com.example.chaostiler.MainActivity.Companion.width


fun clonePixelData() {
    pixelDataClone = pixelData.Clone()
}

fun prepareBlurData(){
    var widewidth = width + 2
    var wideheight = height + 2
    var widehitarray = IntArray(widewidth * wideheight)
    var array = pixelDataClone.aPixelArray

    var posy = width * (height - 1)
    var wposy = widewidth * (wideheight - 1)
    for (x in 0 until width){
        widehitarray[1 + x] = array[posy + x]
        widehitarray[wposy + 1 + x] = array[x]
    }

    var posx = width - 1
    var wposx = widewidth * 2 - 1
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
    var hits = 0.0
    var valu = 0
    posx = 0
    for (y in 1 until widewidth - 1){
        wposy = widewidth * y
        for (x in 1 until wideheight - 1){
            hits = Math.pow(widehitarray[wposy + x].toDouble(), 2.0) * 4.0
            hits += Math.pow(widehitarray[wposy + x - 1].toDouble(), 2.0)
            hits += Math.pow(widehitarray[wposy + x + 1].toDouble(), 2.0)
            hits += Math.pow(widehitarray[wposy + x - widewidth].toDouble(), 2.0)
            hits += Math.pow(widehitarray[wposy + x + widewidth].toDouble(), 2.0)

            hits /= 8.0

            valu = Math.sqrt(hits).toInt()

            array[posx++] = valu

            if(valu > max) max = valu
        }
    }

    pixelDataClone.mMaxHits = max
}
