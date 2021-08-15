package com.fractal.tiler

// region Variable Declaration

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.mSeekbarMax
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val mMaximumColorRangeSeekbarProgress = mSeekbarMax

data class DRgbDataItem(val range: Int = 0, val color: Int = 0)
data class DHsvDataItem(val range: Int = 0, val color: FloatArray)

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

        private val mPrimaryColourCount = 21

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

        lateinit var aCurrentRange : ColorRangeClass

        private var mCurrentRangeCount = 0

        private var mColorRangeList = mutableListOf<ColorRangeClass>()


    init {
        addColorRanges(listOf(
            DRgbDataItem(0, Color.BLACK),
            DRgbDataItem(96, Color.argb(255, 96, 96, 96)),
            DRgbDataItem(160, Color.argb(255, 128, 128, 128)),
            DRgbDataItem(288, Color.argb(255, 224, 224, 224)),
            DRgbDataItem(511, Color.WHITE)),
            DataProcess.STATISTICAL)

        addColorRanges(listOf(
                 DRgbDataItem(0, Color.BLACK),
                 DRgbDataItem(32, Color.RED),
                 DRgbDataItem(96, Color.YELLOW),
                 DRgbDataItem(255, Color.WHITE)))

        addColorRanges(listOf(
                DRgbDataItem(0, Color.argb(255, 0, 0, 32)),
                DRgbDataItem(32, Color.CYAN),
                DRgbDataItem(96, Color.RED),
                DRgbDataItem(255, Color.GREEN)))

        setCurrentColorRange(mCurrentRangeID)
    }


    fun increaseSpreadID() : ColorRangeClass{
        mCurrentRangeID++

        if (mCurrentRangeID > mColorRangeList.size - 1)
            mCurrentRangeID = 0

        setCurrentColorRange(mCurrentRangeID)

        return aCurrentRange
    }

    fun decreaseSpreadID() : ColorRangeClass{
        mCurrentRangeID--

        if (mCurrentRangeID < 0)
            mCurrentRangeID = mColorRangeList.size - 1

        setCurrentColorRange(mCurrentRangeID)

        return aCurrentRange
    }

    fun addNewRandomPrimariesRange() : ColorRangeClass{
        var dataitemcount = MainActivity.rand.nextInt(2, 5)

        var range = 0; var add = 32

        val dataitemslist = ArrayList<DRgbDataItem>(dataitemcount + 1)

        do{
            dataitemslist.add(DRgbDataItem(range,
                aPrimaries.colors[MainActivity.rand.nextInt(0,  aPrimaries.size)]))

            range+= add

            add *= 3

        }while(--dataitemcount > 0)

        addColorRanges(dataitemslist)

        mCurrentRangeID = mColorRangeList.count() - 1

        setCurrentColorRange(mCurrentRangeID)

        return aCurrentRange
    }

    fun addNewRandomColorsRange() : ColorRangeClass {
        var counter = MainActivity.rand.nextInt(3, 6)

        var range = 0

        var colorDataItem = DRgbDataItem(range, Color.BLACK)

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var mult = 1
        var mmax : Int
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

            mmax = max(df1, max(df2, df3)) * mult

            range += mmax

            mult *= 3

            colorDataItem = DRgbDataItem(range, color)

            tempColorDataItemList.add(colorDataItem)
        }while(--counter > 0)

        addColorRanges(tempColorDataItemList)

        mColorRangeList.lastIndex.also { mCurrentRangeID = it }

        setCurrentColorRange(mCurrentRangeID)

        return aCurrentRange
    }

    private fun df(d1 : Int, d2 : Int) : Int{
        return abs(d1 - d2)
    }

    private fun setCurrentColorRange(rangeID : Int){
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

    private fun addColorRanges(colorRanges: List<DRgbDataItem>, dataprocess : DataProcess = DataProcess.LINEAR){
        mColorRangeList.add(ColorRangeClass(mCurrentRangeCount, colorRanges, dataprocess))

        mCurrentRangeCount++
    }

}

class ColorRangeClass(id : Int, colorDataList: List<DRgbDataItem>, dataprocess : DataProcess) {

    // region Variable Declaration

    var progressIncrement = mMaximumColorRangeSeekbarProgress
    var progressSecond = mMaximumColorRangeSeekbarProgress
    var progressStatistic = mMaximumColorRangeSeekbarProgress

    var mColorRangeID = id

    var dataProcess = dataprocess

    private val mColorDataList = colorDataList

    val mColorSpreadCount = colorDataList.last().range

    var aColorSpread : IntArray = IntArray(mColorSpreadCount + 1)

    // endregion

    init {
        processColorSpread()
    }

    fun getRangeProgress() : Int{
        if (dataProcess == MainActivity.Companion.DataProcess.LINEAR)
            return progressIncrement

        return progressStatistic
    }

    fun setRangeProgress(prog : Int){
        if (dataProcess == MainActivity.Companion.DataProcess.LINEAR){
            progressIncrement = prog
        }
        else{
            progressStatistic = prog
        }
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
