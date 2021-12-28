package com.fractal.tiler



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


    fun doImageFilter(pixelDataCopy : PixelData){
        val wideArray = getWideArray(pixelDataCopy.aHitsArray)

        pixelDataCopy.aHitsArray = performFunction(wideArray)

        pixelDataCopy.recalcHitStats()
    }

}

fun Filter.performFunction(wideArray: Array<IntArray>) : IntArray{

    val array = IntArray(MainActivity.width * MainActivity.height)

    var hits : Int

    var middlehit : Int

    var i = 0

    if (weight == -1) {
        val kernsize = kernWid * kernHit

        val hitlist = mutableListOf<Int>()
        val midindex = kernsize / 2
        var index : Int
        var ik : Int
        for (y in 0 until MainActivity.height) {
            for (x in 0 until MainActivity.width) {
                hitlist.clear()
                index = 0
                for (ky in 0 until kernHit) {
                    for (kx in 0 until kernWid) {
                        ik = 0
                        hits = wideArray[y + ky][x + kx] * kernel[ky][kx]
                        while(ik < index && hits > hitlist[ik]) ik++
                        hitlist.add(ik, hits)
                        index++
                    }
                }

                middlehit = wideArray[y + offBot][x + offRight]

                array[i++] = middlehit + ((hitlist[midindex] - middlehit) * 0.75F).toInt()
            }
        }
    }
    else {
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