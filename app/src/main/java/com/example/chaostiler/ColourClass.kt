package com.example.chaostiler

// region Variable Declaration

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.chaostiler.MainActivity.Companion.DataProcess
import com.example.chaostiler.MainActivity.Companion.mSeekbarMax
import kotlin.math.abs
import kotlin.math.max

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
            DRgbDataItem(160, Color.argb(255, 128, 128, 128)),
            DRgbDataItem(255, Color.WHITE)))

        addColorRanges(listOf(
                 DRgbDataItem(0, Color.BLACK),
                 DRgbDataItem(160, Color.RED),
                 DRgbDataItem(224, Color.YELLOW),
                 DRgbDataItem(255, Color.WHITE)))

        addColorRanges(listOf(
                DRgbDataItem(0, Color.BLUE),
                DRgbDataItem(160, Color.RED),
                DRgbDataItem(224, Color.GREEN),
                DRgbDataItem(255, Color.WHITE)))

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
        var dataitemcount = 2 + MainActivity.rand.nextInt(0, 4)

        var range = 0; var add = 64

        val dataitemslist = ArrayList<DRgbDataItem>(dataitemcount + 1)

        if (dataitemcount == 2) add = 256
        if (dataitemcount == 3) add = 128

        dataitemslist.add(DRgbDataItem(range, Color.BLACK))

        do{
            range+= add

            add *= 2

            dataitemslist.add(DRgbDataItem(range,
                aPrimaries.colors[MainActivity.rand.nextInt(0,  aPrimaries.size)]))

        }while(--dataitemcount > 0)

        addColorRanges(dataitemslist)

        mCurrentRangeID = mColorRangeList.count() - 1

        setCurrentColorRange(mCurrentRangeID)

        return aCurrentRange
    }

    fun addNewRandomColorsRange() : ColorRangeClass {
        var counter = MainActivity.rand.nextInt(0, 5) * 128 + 256

        var colorDataItem = DRgbDataItem(0, Color.BLACK)

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

            colorDataItem = DRgbDataItem(range, color)

            tempColorDataItemList.add(colorDataItem)

            counter -= max
        }while(counter > 0)

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

    private fun addColorRanges(colorRanges: List<DRgbDataItem>){
        mColorRangeList.add(ColorRangeClass(mCurrentRangeCount, colorRanges))

        mCurrentRangeCount++
    }

}

class ColorRangeClass(id : Int, colorDataList: List<DRgbDataItem>) {

    // region Variable Declaration

    var progressIncrement = mMaximumColorRangeSeekbarProgress
    var progressSecond = mMaximumColorRangeSeekbarProgress
    var progressStatistic = mMaximumColorRangeSeekbarProgress

    var mColorRangeID = id

    var dataProcess = DataProcess.LINEAR

    private val mColorDataList = colorDataList

    val mColorSpreadCount = colorDataList.last().range

    var aColorSpread : IntArray = IntArray(mColorSpreadCount + 1)

    // endregion

    init {
        processColorSpread()
        //processHsvColorSpread()
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


    private fun processHsvColorSpread() {
        if (mColorDataList.size < 2) return

        var index = 1

        var col1: Int
        var col2: Int

        aColorSpread[0] = mColorDataList[0].color

        for (cr in 1 until mColorDataList.size) {
            val maxd: Int = mColorDataList[cr].range - mColorDataList[cr- 1].range

            col1 = mColorDataList[cr - 1].color
            col2 = mColorDataList[cr].color

            val cols: IntArray = hsvColorPairToRGBIntArray(maxd, col1, col2)

            for (n in 0..cols.lastIndex) {
                aColorSpread[index] = cols[n]
                index++
            }
        }
    }

    private fun hsvColorPairToRGBIntArray(steps : Int, rgb1 : Int, rgb2 : Int) : IntArray{
        var hsv1 = FloatArray(3)
        Color.colorToHSV(rgb1, hsv1)

        var hsv2 = FloatArray(3)
        Color.colorToHSV(rgb2, hsv2)

        val h1 = hsv1[0]
        var hdif = hsv2[0] - h1

        if (hdif > 0){
            if (hdif > 180){
                hdif -= 360
            }
        } else{
            if (hdif < -180){
                hdif += 360
            }
        }

        val oneoversteps = 1.0F / steps

        val addh = hdif * oneoversteps
        var hres : Float

        val s1 = hsv1[1]
        val adds = (hsv2[1] - s1) * oneoversteps
        val v1 = hsv1[2]
        val addv = (hsv2[2] - v1) * oneoversteps

        val cols = IntArray(steps)

        for (i in 1..steps){
            hres = h1 + addh * i
            if(hres < 360){
                if (hres < 0){
                    hres += 360
                }
            } else{
                hres -= 360
            }

            cols[i - 1] = Color.HSVToColor(floatArrayOf(hres, s1 + i * adds, v1 + i * addv))
        }

        return  cols
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
