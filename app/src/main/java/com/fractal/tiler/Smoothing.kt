package com.fractal.tiler

import android.graphics.Color
import com.fractal.tiler.MainActivity.Companion.colorClass


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

    fun doImageFilter() : IntArray{
        val wideArray = getWideArray(aColors)

        performColorFunction(wideArray).copyInto(aColors)

        return aColors
    }

    fun doHitsFilter(pixelDataCopy : PixelData){
        val wideArray = getWideArray(pixelDataCopy.aHitsArray)

        performFunction(wideArray).copyInto(pixelDataCopy.aHitsArray)

        pixelDataCopy.recalcScaledHitStats()
    }

}

fun Filter.performColorFunction(wideArray: Array<IntArray>) : IntArray{

    val array = IntArray(MainActivity.width * MainActivity.height)

    var color : Int
    var red : Int
    var green : Int
    var blue : Int
    var i = 0

    if (weight < 0) { // Smooth Filter
        for (y in 0 until MainActivity.height) {
            for (x in 0 until MainActivity.width) {
                red = 0
                green = 0
                blue = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        if (kernel[ky][kx] == 1) {
                            color = wideArray[y + ky][x + kx]
                            red += Color.red(color)
                            green += Color.green(color)
                            blue += Color.blue(color)
                        }
                    }
                }

                val wt = -1.0f / weight

                array[i++] = Color.argb(
                    255, (red * wt).toInt(), (green * wt).toInt(), (blue * wt).toInt())
            }
        }
    } else {
        val wt = 1.0F / weight
        var kern : Int
        for (y in 0 until MainActivity.height) {
            for (x in 0 until MainActivity.width) {
                red = 0
                green = 0
                blue = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        color = wideArray[y + ky][x + kx]
                        kern = kernel[ky][kx]
                        red += Color.red(color) * kern
                        green += Color.green(color) * kern
                        blue += Color.blue(color) * kern
                    }
                }

                array[i++] = Color.argb(
                    255, (red * wt).toInt(), (green * wt).toInt(), (blue * wt).toInt())
            }
        }
    }

    return array
}

fun Filter.performFunction(wideArray: Array<IntArray>) : IntArray{

    val array = IntArray(MainActivity.width * MainActivity.height)

    var hits : Int

    var center : Int
    var i = 0

    if (weight < 0) { // Smooth Filter MUST BE kernel values of 0 or 1
        val wt = -1.0F / weight
        if (weight == -8) {
            for (y in 0 until MainActivity.height) {
                for (x in 0 until MainActivity.width) {
                    hits = 0
                    center = 0
                    for (ky in 0 until kernHit) {
                        for (kx in 0 until kernWid) {
                            if (kernel[ky][kx] == 1)
                              hits += wideArray[y + ky][x + kx]
                            else center = wideArray[y + ky][x + kx]
                           // hits += (wideArray[y + ky][x + kx] * kernel[ky][kx])
                        }
                    }

                    array[i++] = center + (((hits * wt) - center) * 0.5f).toInt()
                }
            }
        } else{//weight == -9
            for (y in 0 until MainActivity.height) {
                for (x in 0 until MainActivity.width) {
                    hits = 0
                    for (ky in 0 until kernHit) {
                        for (kx in 0 until kernWid) {
                            hits += wideArray[y + ky][x + kx]
                        }
                    }

                    array[i++] = (hits * wt) .toInt()
                }
            }
        }
    } else {
        val wt = 1.0F / weight
        for (y in 0 until MainActivity.height) {
            for (x in 0 until MainActivity.width) {
                hits = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        hits += wideArray[y + ky][x + kx] * kernel[ky][kx]
                    }
                }

                array[i++] = (hits * wt).toInt()
            }
        }
    }

    return array
}

fun Filter.getWideArray(array : IntArray) : Array<IntArray>{
    val widewidth = MainActivity.width + offLeft + offRight
    val wideheight = MainActivity.height + offTop + offBot
    val wideArray = Array(wideheight) { IntArray(widewidth)}

    var i = 0
    var wposy = widewidth * offTop

    for (y in 0 until MainActivity.height){
        for (x in 0 until MainActivity.width){
            wideArray[offBot + y][offLeft + x] = array[i++]
        }
        wposy += widewidth
    }

    i = 0
    for (wh in MainActivity.height until wideheight) {
        for (x in 0 until MainActivity.width) {
            wideArray[wh][offLeft + x] = array[i++]
        }
    }

    for (wh in 0 until offBot) {
        wposy = MainActivity.width * (MainActivity.height - offBot + wh)
        for (x in 0 until MainActivity.width) {
            wideArray[wh][offLeft + x] = array[wposy + x]
        }
    }

    for (y in 0 until wideheight) {
        for (ww in 0 until offLeft) {
            wideArray[y][ww] = wideArray[y][MainActivity.width + ww]
            wideArray[y][offLeft + MainActivity.width + ww] = wideArray[y][offLeft + ww]
        }
    }

    return wideArray
}