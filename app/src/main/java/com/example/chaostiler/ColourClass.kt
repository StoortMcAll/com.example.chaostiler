package com.example.chaostiler

// region Variable Declaration

import android.graphics.Color
import android.widget.ImageButton
import java.time.LocalDateTime
import kotlin.random.Random

data class dColorDataItem(val range: Int = 0, val color: Int = 0)

data class dPrimaryColors(val size : Int, val colors : IntArray)

// endregion


class ColorClass {

    // region Variable Declaration

    companion object {
        private const val mPrimaryColourCount = 21

        private var isQueuedChange = true
        private var mQueuedID = 0

        private val aPrimarys = dPrimaryColors(mPrimaryColourCount, intArrayOf(
                Color.RED, Color.GREEN, Color.BLUE,

                Color.argb(255, 255,255, 0),
                Color.argb(255, 255,0, 255),
                Color.argb(255, 0,255, 255),

                Color.argb(255, 255,128, 0),
                Color.argb(255, 255,0, 128),
                Color.argb(255, 128,255, 0),

                Color.argb(255, 0,255, 128),
                Color.argb(255, 128,0, 255),
                Color.argb(255, 0,128, 255),

                Color.argb(255, 255,64, 64),
                Color.argb(255, 64,255, 64),
                Color.argb(255, 64,64, 255),

                Color.argb(255, 160,32, 64),
                Color.argb(255, 64,160, 32),
                Color.argb(255, 32,64, 160),

                Color.argb(255, 96,32, 12),
                Color.argb(255, 12,96, 32),
                Color.argb(255, 32,12, 96)
            ))

        private var mCurrentRangeID = 0

        private lateinit var aCurrentRange : ColorRangeClass

        private var mCurrentRangeCount = 0

        private var mColorRangeList = mutableListOf<ColorRangeClass>()

    }

    // endregion


    init {
        add_ColorRange(listOf(
                 dColorDataItem(0, Color.BLACK),
                 dColorDataItem(128, Color.RED),
                 dColorDataItem(192, Color.YELLOW),
                 dColorDataItem(255, Color.WHITE)))

        add_ColorRange(listOf(
                dColorDataItem(0, Color.BLACK),
                dColorDataItem(128, Color.BLUE),
                dColorDataItem(192, Color.MAGENTA),
                dColorDataItem(255, Color.WHITE)))

        //set_CurrentColorRange_to(0)
    }


    fun getCurrentRange() : ColorRangeClass{
        if (isQueuedChange){
            set_CurrentColorRange_to(mQueuedID)
        }
        return aCurrentRange
    }


    private fun set_CurrentColorRange_to(rangeID : Int){
        var index = 0
        do {
            if (mColorRangeList[index].mColorRangeID == rangeID){
                mCurrentRangeID = rangeID
                aCurrentRange = mColorRangeList[index]

                return
            }
            index++
        }while (index < mColorRangeList.size)

        // If rangeID not valid set to range 0
        aCurrentRange = mColorRangeList[0]
        mCurrentRangeID = aCurrentRange.mColorRangeID
    }


    fun Increase_SpreadID(){
        mQueuedID = mCurrentRangeID + 1

        if (mQueuedID > mColorRangeList.size - 1)
            mQueuedID = 0

        isQueuedChange = true
    }

    fun Decrease_SpreadID(){
        mQueuedID = mCurrentRangeID - 1

        if (mQueuedID < 0)
            mQueuedID = mColorRangeList.size - 1

        isQueuedChange = true
    }

    fun addNew_RandomPrimarysRange() {
        var dataitemcount = 2 + MainActivity.rand.nextInt(0, 4)

        var range = 0; var add = 64

        var dataitemslist = ArrayList<dColorDataItem>(dataitemcount + 1)

        if (dataitemcount == 2) add = 256
        if (dataitemcount == 3) add = 128

        dataitemslist.add(dColorDataItem(range, Color.BLACK))

        do{
            range+= add

            add *= 2

            dataitemslist.add(dColorDataItem(range,
                aPrimarys.colors[MainActivity.rand.nextInt(0,  aPrimarys.size)]))

        }while(--dataitemcount > 0)

        add_ColorRange(dataitemslist)

        mQueuedID = mColorRangeList.count() - 1

        isQueuedChange = true
    }


    private fun add_ColorRange(colorRanges: List<dColorDataItem>){
        mColorRangeList.add(ColorRangeClass(mCurrentRangeCount, colorRanges))

        mCurrentRangeCount++
    }


    class ColorRangeClass(id : Int, colorDataList: List<dColorDataItem>) {

        // region Variable Declaration

        var prog = 255

        var mColorRangeID = id

        private val mColorDataList = colorDataList

        val mColorSpreadCount = colorDataList.last().range

        var aColorSpread : IntArray = IntArray(mColorSpreadCount + 1)

        // endregion


        init {

            Process_ColorSpread()
        }


        private fun Process_ColorSpread() {
            if (mColorDataList.size < 2) return;

            var index: Int = 1

            var col1: Int
            var col2: Int

            aColorSpread[0] = mColorDataList[0].color

            for (cr in 1 until mColorDataList.size) {
                val maxd: Int = mColorDataList[cr].range - mColorDataList[cr- 1].range

                col1 = mColorDataList[cr - 1].color
                col2 = mColorDataList[cr].color

                val r: IntArray = Color_Channel_Range(maxd, Color.red(col1), Color.red(col2))
                val g: IntArray = Color_Channel_Range(maxd, Color.green(col1), Color.green(col2))
                val b: IntArray = Color_Channel_Range(maxd, Color.blue(col1), Color.blue(col2))

                var n = 0
                do {
                    aColorSpread[index] = Color.argb(255, r[n], g[n], b[n])
                    index++
                    n++
                }while(index < mColorDataList[cr].range)
            }
        }

        private fun Color_Channel_Range(range: Int, r1: Int, r2: Int): IntArray {
            var results: IntArray = IntArray(range)

            results[0] = r1;

            val dx: Double = (r2 - r1) / range.toDouble()

            if (dx == 0.0) {
                for (i in 1 until range)
                    results[i] = r1
            } else {
                for (i in 1 until range)
                    results[i] = (r1 + (dx * i)).toInt();
            }

            return results;
        }
    }

}