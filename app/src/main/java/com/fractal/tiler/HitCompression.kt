package com.fractal.tiler


data class HitCompressor(var level : Int){

    private val ceiling = Math.pow(level.toDouble(), 3.0).toInt()

    private var hitCoordList = mutableListOf<Int>()

    var counter = 0

    fun addHitCoord(hitCoord : Int) : Boolean{
        hitCoordList.add(hitCoord)

        if (++counter < ceiling) return false

        return true
    }

    fun pullDataCoordList(pixelData: PixelData) {
        val oldMaxHits = pixelData.mMaxHits
        val newMaxHits = oldMaxHits + 1

        var value : Int
        var index : Int

        pixelData.aHitStats.add(0)

        //var hitCompressor = HitCompressor(5)

        pixelData.mMaxHits++

        for (i in 0..hitCoordList.lastIndex){
            index = hitCoordList[i]

            value = pixelData.aHitsArray[index]
            value++

            if (value > newMaxHits)
                //hitCompressor.addHitCoord(index)
            else{
                pixelData.aHitStats[oldMaxHits]--
                pixelData.aHitStats[newMaxHits]++

                pixelData.aHitsArray[index]++

                pixelData.mHitsCount++
            }

        }

        clear()

       // return hitCompressor
    }

    fun clear()
    {
        counter = 0

        hitCoordList = mutableListOf<Int>()
    }
}

