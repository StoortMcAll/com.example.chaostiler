package com.example.chaostiler

// region Variable Declaration

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.abs
import kotlin.math.max

private const val mInitialColorRangeSeekbarProgress = 0

data class DColorDataItem(val range: Int = 0, val color: Int = 0)

data class DPrimaryColors(val size : Int, val colors : IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DPrimaryColors

        if (size != other.size) return false
        if (!colors.contentEquals(other.colors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + colors.contentHashCode()
        return result
    }
}

// endregion


class ColorClass {

    // region Variable Declaration

    companion object {
        private const val mPrimaryColourCount = 21

        private var isQueuedChange = true
        private var mQueuedID = 0

        private val aPrimaries = DPrimaryColors(mPrimaryColourCount, intArrayOf(
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
        addColorRanges(listOf(
            DColorDataItem(0, Color.BLACK),
            DColorDataItem(112, Color.argb(255, 128, 128, 128)),
            DColorDataItem(255, Color.WHITE)))

        addColorRanges(listOf(
                 DColorDataItem(0, Color.BLACK),
                 DColorDataItem(112, Color.RED),
                 DColorDataItem(208, Color.YELLOW),
                 DColorDataItem(255, Color.WHITE)))

        addColorRanges(listOf(
                DColorDataItem(0, Color.BLACK),
                DColorDataItem(112, Color.BLUE),
                DColorDataItem(208, Color.MAGENTA),
                DColorDataItem(255, Color.WHITE)))
    }


    fun getCurrentRange() : ColorRangeClass{
        if (isQueuedChange){
            selectCurrentColorRange(mQueuedID)
            isQueuedChange = false
        }
        return aCurrentRange
    }


    private fun selectCurrentColorRange(rangeID : Int){
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


    fun increaseSpreadID(){
        mQueuedID = mCurrentRangeID + 1

        if (mQueuedID > mColorRangeList.size - 1)
            mQueuedID = 0

        isQueuedChange = true
    }

    fun decreaseSpreadID(){
        mQueuedID = mCurrentRangeID - 1

        if (mQueuedID < 0)
            mQueuedID = mColorRangeList.size - 1

        isQueuedChange = true
    }

    fun addNewRandomPrimariesRange() {
        var dataitemcount = 2 + MainActivity.rand.nextInt(0, 4)

        var range = 0; var add = 64

        val dataitemslist = ArrayList<DColorDataItem>(dataitemcount + 1)

        if (dataitemcount == 2) add = 256
        if (dataitemcount == 3) add = 128

        dataitemslist.add(DColorDataItem(range, Color.BLACK))

        do{
            range+= add

            add *= 2

            dataitemslist.add(DColorDataItem(range,
                aPrimaries.colors[MainActivity.rand.nextInt(0,  aPrimaries.size)]))

        }while(--dataitemcount > 0)

        addColorRanges(dataitemslist)

        mQueuedID = mColorRangeList.count() - 1

        isQueuedChange = true
    }


    fun addNewRandomColorsRange() {
        var counter = MainActivity.rand.nextInt(0, 5) * 128 + 256

        var colorDataItem = DColorDataItem(0, Color.BLACK)

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var range = 0

        var max : Int
        do{
            val r1 = colorDataItem.color.red
            val g1 = colorDataItem.color.green
            val b1 = colorDataItem.color.blue

            val r2 = MainActivity.rand.nextInt(0, 256)
            val g2 = MainActivity.rand.nextInt(0, 256)
            val b2 = MainActivity.rand.nextInt(0, 256)

            val color = Color.argb(255, r2, g2, b2)

            val df1 = df(r1, r2)
            val df2 = df(g1, g2)
            val df3 = df(b1, b2)

            max = max(max(df1, df2), df3)

            range += max

            colorDataItem = DColorDataItem(range, color)

            tempColorDataItemList.add(colorDataItem)

            counter -= max
        }while(counter > 0)

        addColorRanges(tempColorDataItemList)

        mColorRangeList.lastIndex.also { mQueuedID = it }

        isQueuedChange = true
    }

    private fun df(d1 : Int, d2 : Int) : Int{
        return abs(d1 - d2)
    }


    private fun addColorRanges(colorRanges: List<DColorDataItem>){
        mColorRangeList.add(ColorRangeClass(mCurrentRangeCount, colorRanges))

        mCurrentRangeCount++
    }


    class ColorRangeClass(id : Int, colorDataList: List<DColorDataItem>) {

        // region Variable Declaration

        var prog = mInitialColorRangeSeekbarProgress

        var mColorRangeID = id

        var dataProcess = MainActivity.Companion.DataProcess.LINEAR

        private val mColorDataList = colorDataList

        val mColorSpreadCount = colorDataList.last().range

        var aColorSpread : IntArray = IntArray(mColorSpreadCount + 1)

        // endregion

        init {
            processColorSpread()
        }


        private fun processColorSpread() {
            if (mColorDataList.size < 2) return

            var index = 1

            var col1: Int
            var col2: Int

            aColorSpread[0] = mColorDataList[0].color
            aColorSpread[aColorSpread.lastIndex] = mColorDataList[mColorDataList.lastIndex].color

            for (cr in 1 until mColorDataList.size) {
                val maxd: Int = mColorDataList[cr].range - mColorDataList[cr- 1].range

                col1 = mColorDataList[cr - 1].color
                col2 = mColorDataList[cr].color

                val r: IntArray = colorChannelRange(maxd, Color.red(col1), Color.red(col2))
                val g: IntArray = colorChannelRange(maxd, Color.green(col1), Color.green(col2))
                val b: IntArray = colorChannelRange(maxd, Color.blue(col1), Color.blue(col2))

                var n = 0
                do {
                    aColorSpread[index] = Color.argb(255, r[n], g[n], b[n])
                    index++
                    n++
                }while(index < mColorDataList[cr].range)
            }
        }

        private fun colorChannelRange(range: Int, r1: Int, r2: Int): IntArray {
            val results = IntArray(range)

            results[0] = r1

            val dx: Double = (r2 - r1) / range.toDouble()

            if (dx == 0.0) {
                for (i in 1 until range)
                    results[i] = r1
            } else {
                for (i in 1 until range)
                    results[i] = (r1 + (dx * i)).toInt()
            }

            return results
        }
    }

}