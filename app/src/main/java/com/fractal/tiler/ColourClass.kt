package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.red
import com.fractal.tiler.MainActivity.Companion.mColorRangeLastIndex
import com.fractal.tiler.MainActivity.Companion.rand
import com.fractal.tiler.MainActivity.Companion.myResources

private const val mMaximumColorRangeSeekbarProgress = mColorRangeLastIndex

data class RangeColorsList(val size : Int){
    val ranges = FloatArray(size)
    val colors = IntArray(size)

    constructor(colorDataList: List<RangeColorDataItem>) : this(colorDataList.size){
        for (i in 0 until size){
            ranges[i] = colorDataList[i].range
            colors[i] = colorDataList[i].color
        }
    }

    fun setValue(index : Int, range : Float){
        if (index < 0 || index >= size) return

        ranges[index] = range
    }
    fun setValue(index : Int, color : Int){
        if (index < 0 || index >= size) return

        colors[index] = color
    }
    fun setDataItem(index : Int, dataItem: RangeColorDataItem){
        if (index < 0 || index >= size) return

        ranges[index] = dataItem.range
        colors[index] = dataItem.color
    }

    fun getDataItem(index : Int) : RangeColorDataItem {
        val i =
            if (index < 0 || index >= size) 0
            else index

        return RangeColorDataItem(ranges[index], colors[index])
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RangeColorsList

        if (size != other.size) return false

        for (i in 0 until size) {
            if (ranges[i] != other.ranges[i]) return false
            if (colors[i] != other.colors[i]) return false
        }
        return true
    }
}
data class RangeColorDataItem(var range : Float, var color : Int){

    fun copy() : RangeColorDataItem {
        return RangeColorDataItem(range, color)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RangeColorDataItem

        if (range != other.range) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        return (31 * range).toInt() + color
    }
}

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

/**
 * Handles all colorRanges
 */
class ColorClass {

    // region Variable Declaration

    private val mPrimaryColourCount = 27

    private val aPrimaries = DPrimaryColors(mPrimaryColourCount, intArrayOf(
        Color.WHITE,

        Color.argb(255, 255,0, 0),
        Color.argb(255, 255,125, 0),
        Color.argb(255, 255,255, 0),

        Color.argb(255, 125,255, 0),
        Color.argb(255, 0,255, 0),
        Color.argb(255, 0,255, 125),

        Color.argb(255, 0,255, 255),
        Color.argb(255, 0,125, 255),
        Color.argb(255, 0,0, 255),

        Color.argb(255, 125,0, 255),
        Color.argb(255, 255,0, 255),
        Color.argb(255, 255,0, 125),

        Color.argb(255, 32,0, 0),
        Color.argb(255, 32,10, 0),
        Color.argb(255, 32,32, 0),

        Color.argb(255, 10,32, 0),
        Color.argb(255, 0,32, 0),
        Color.argb(255, 0,32, 10),

        Color.argb(255, 0,32, 32),
        Color.argb(255, 0,10, 32),
        Color.argb(255, 0,0, 32),

        Color.argb(255, 10,0, 32),
        Color.argb(255, 32,0, 32),
        Color.argb(255, 32,0, 10),

        Color.GRAY, Color.BLACK
    ))

    lateinit var aPrevRange : ColorRangeClass
    lateinit var aCurrentRange : ColorRangeClass
    lateinit var aNextRange : ColorRangeClass

    private var mRangeIndexCounter = 0
    private var mCurrentRangeIndex = 0

    var mColorRangeList = mutableListOf<ColorRangeClass>()

    var mNewColors = true

    // endregion

    init {
        addColorRanges(listOf(
                 RangeColorDataItem(0.0F, Color.YELLOW),//Color.argb(255, 32, 0, 0)),
                 RangeColorDataItem(0.25F, Color.RED),
                 RangeColorDataItem(0.5F, Color.BLACK),
                 RangeColorDataItem(0.75F, Color.RED),
                RangeColorDataItem(1.0F, Color.YELLOW)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.BLACK),//Color.argb(255, 32, 0, 0)),
            RangeColorDataItem(0.25F, Color.WHITE),
            RangeColorDataItem(0.5F, Color.BLACK),
            RangeColorDataItem(0.75F, Color.argb(255, 255, 119, 0)),
            RangeColorDataItem(1.0F, Color.BLACK)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.argb(255, 0, 0, 0)),
            RangeColorDataItem(0.13F, Color.argb(255, 85, 85, 85)),
            RangeColorDataItem(0.35F, Color.argb(255, 170, 170, 170)),
            RangeColorDataItem(1.0F, Color.WHITE)))

        addColorRanges(listOf(
                RangeColorDataItem(0.0F, Color.argb(255, 0, 0, 0)),
                RangeColorDataItem(0.15F, Color.argb(255, 0, 0, 255)),
                RangeColorDataItem(0.35F, Color.argb(255, 0, 0, 0)),
                RangeColorDataItem(0.60F, Color.argb(255, 255, 255, 0)),
                RangeColorDataItem(1.0F, Color.argb(255, 0, 0, 0))))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.argb(255, 0, 0, 0)),
            RangeColorDataItem(0.25F, Color.argb(255, 255, 255, 255)),
            RangeColorDataItem(1.0F, Color.BLACK)))

        setCurrentColorRange(0)
    }

    fun setProgress(prog: Int) {
        aCurrentRange.setRangeProgress(prog)

        mNewColors = true
    }

    fun selectNextColorRange() : ColorRangeClass{
        mCurrentRangeIndex++

        if (mCurrentRangeIndex == mColorRangeList.size)
            mCurrentRangeIndex = 0

        setCurrentColorRange(mCurrentRangeIndex)

        mNewColors = true

        return aCurrentRange
    }

    fun selectPrevColorRange() : ColorRangeClass{
        mCurrentRangeIndex--

        if (mCurrentRangeIndex == -1)
            mCurrentRangeIndex = mColorRangeList.lastIndex

        setCurrentColorRange(mCurrentRangeIndex)

        mNewColors = true

        return aCurrentRange
    }

    /**
    Add new HSV colorRange
     */
    fun addNewColorA() : ColorRangeClass {
        aCurrentRange = addNewRandomHSVRange()

        setCurrentColorRange(aCurrentRange.mColorRangeID)

        mNewColors = true

        return aCurrentRange
    }
    /**
    Add new RGB colorRange
     */
    fun addNewColorB() : ColorRangeClass {
        aCurrentRange = addNewRandomPrimariesRange()

        setCurrentColorRange(aCurrentRange.mColorRangeID)

        mNewColors = true

        return aCurrentRange
    }

    private fun addNewRandomPrimariesRange() : ColorRangeClass{
        val dataitemcount = rand.nextInt(2, 5)

        val add = 1.0F / dataitemcount

        val color : Int
        var colorIndex : Int
        var lastColorIndex : Int

        if (rand.nextBoolean())
            lastColorIndex = mPrimaryColourCount - 1
        else
            lastColorIndex = rand.nextInt(0, mPrimaryColourCount)
        color = aPrimaries.colors[lastColorIndex]


        val tempColorDataItemList = mutableListOf(RangeColorDataItem(0.0F, color))
        for (i in 1..dataitemcount){
            do {
                colorIndex = rand.nextInt(0, mPrimaryColourCount)
            }while (colorIndex == lastColorIndex || (lastColorIndex > 12 && colorIndex > 12))

            tempColorDataItemList.add(
                RangeColorDataItem(i * add, aPrimaries.colors[colorIndex]))

            lastColorIndex = colorIndex
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addNewRandomHSVRange() : ColorRangeClass {
        var dataitemcount = rand.nextInt(2, 5)

        val add = 1.0F / dataitemcount

        var isDifPos = rand.nextBoolean()
        val angle = rand.nextInt(0, 256)

        var isLumHi = rand.nextInt(4) < 2
        var lum = if (isLumHi) 255 else 8

        var color = Color.argb(if (isDifPos) 128 else 0, angle, 255, lum)

        var colorDataItem = RangeColorDataItem(0.0F, color)

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var dif : Int
        for (i in 1..dataitemcount){
            dif = rand.nextInt(32, 64)
            isDifPos = rand.nextBoolean()

            isLumHi = if (isLumHi) rand.nextInt(4) < 2 else true
            lum = if (isLumHi) 255 else rand.nextInt(0, 64)

            color = Color.argb(if (isDifPos) 128 else 0, dif, 255, lum)

            colorDataItem = RangeColorDataItem(i * add, color)

            tempColorDataItemList.add(colorDataItem)
        }

        addColorRanges(tempColorDataItemList, true)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addColorRanges(colorRanges: List<RangeColorDataItem>, isHSV : Boolean = false){
        mColorRangeList.add(ColorRangeClass(mRangeIndexCounter++, colorRanges, isHSV))
    }

    private fun setCurrentColorRange(rangeID : Int){
        var index = 0
        var invalid = true

        do {
            if (mColorRangeList[index].mColorRangeID == rangeID){
                mCurrentRangeIndex = rangeID
                invalid = false
            }
            else {
                if (++index == mColorRangeList.size){
                    mCurrentRangeIndex = 0
                    invalid = false
                }
            }
        }while (invalid)

        aCurrentRange = mColorRangeList[mCurrentRangeIndex]
        aCurrentRange.calcProgress()

        index = mCurrentRangeIndex + 1
        if (index == mColorRangeList.size)
            aNextRange = mColorRangeList[0]
        else
            aNextRange = mColorRangeList[index]

        index = mCurrentRangeIndex - 1
        if (index == -1)
            aPrevRange = mColorRangeList[mColorRangeList.lastIndex]
        else
            aPrevRange = mColorRangeList[index]
    }

}

/**
 * Contains info on each unique colorRange
 */
class ColorRangeClass(id : Int, private val aColorDataList: List<RangeColorDataItem>, var isHSV: Boolean = false) {

    // region Variable Declaration

    var progressIncrement = mMaximumColorRangeSeekbarProgress

    var mColorRangeID = id

    var mActiveColorIndex = 1

    //val aColorDataList = colorDataList

    val colorRangeBitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    var colorButtonBitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)

    var colorRangeDrawable = RoundedBitmapDrawableFactory.create(myResources, colorRangeBitmap)
    lateinit var colorButtonDrawable : RoundedBitmapDrawable

    var aColorSpread = IntArray(mColorRangeLastIndex + 1)

   // val aGradientPercentage = floatArrayOf(0.0f, 0.5f, 1.0f)

    val aRgbColorsList = IntArray(aColorDataList.size)

    var lhsColorProgress = 0
    var rhsColorProgress = 100
    var seekbarMax = 100

    // endregion

    init {
        calcProgress()

        if (isHSV) processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
    }


    fun setActiveColorId(colorId : Int){
        val oldId = mActiveColorIndex
        mActiveColorIndex = colorId

        calcProgress()

        if (oldId != colorId) updateButtonBitmap()
    }

    fun calcProgress() : Int{
        lhsColorProgress = (mColorRangeLastIndex * aColorDataList[mActiveColorIndex - 1].range).toInt()
        progressIncrement = (mColorRangeLastIndex * aColorDataList[mActiveColorIndex].range).toInt() - lhsColorProgress
        rhsColorProgress = (mColorRangeLastIndex * aColorDataList[mActiveColorIndex + 1].range).toInt()
        seekbarMax = rhsColorProgress - lhsColorProgress

        if (progressIncrement == 0) progressIncrement = 1
        else if (progressIncrement == seekbarMax) progressIncrement = seekbarMax - 1

        return progressIncrement
    }

    fun setRangeProgress(prog : Int){
        progressIncrement = prog
        if (progressIncrement == 0) progressIncrement = 1
        else if (progressIncrement == seekbarMax) progressIncrement = seekbarMax - 1

        val range = (lhsColorProgress + progressIncrement) * (1.0F / mColorRangeLastIndex.toFloat())

        aColorDataList[mActiveColorIndex].range = range

        if (isHSV) processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
    }

    fun updateColorSpreadBitmap() {
        updateButtonBitmap()

        updateRangeBitmap()
    }


    private fun updateRangeBitmap() {
        colorRangeBitmap.setPixels(
            aColorSpread,
            0,
            colorRangeBitmap.width,
            0,
            0,
            colorRangeBitmap.width,
            1
        )
        colorRangeDrawable = RoundedBitmapDrawableFactory.create(myResources, colorRangeBitmap)
        colorRangeDrawable.cornerRadius = MainActivity.dpToPx
    }
    private fun updateButtonBitmap() {
        colorButtonBitmap = Bitmap.createBitmap(
            aColorSpread,
            lhsColorProgress,
            mColorRangeLastIndex + 1,
            seekbarMax,
            1,
            Bitmap.Config.ARGB_8888
        )
        colorButtonDrawable = RoundedBitmapDrawableFactory.create(myResources, colorButtonBitmap)
        colorButtonDrawable.cornerRadius = MainActivity.dpToPx
    }


    private fun processHSVSpread() {
        if (aColorDataList.size < 2) return

        var isDifPos: Boolean
        var maxRange: Int
        var difAngle : Float
        var difLum : Float
        var lum2: Float
        var colorData : Int

        var index = 0
        val bin2Rad = 360.0f / 255.0f

        var angle = aColorDataList[0].color.red * bin2Rad
        var lum = aColorDataList[0].color.blue / 255.0f

        for (cr in 1 until aColorDataList.size) {
            colorData = aColorDataList[cr].color

            maxRange = (aColorDataList[cr].range * mColorRangeLastIndex).toInt()

            isDifPos = colorData.alpha == 128
            difAngle = (colorData.red * bin2Rad) / (maxRange - index)

            lum2 = colorData.blue / 255.0f
            difLum = lum2 - lum
            if (difLum != 0.0F) difLum /= (maxRange - index)

            if (isDifPos) {
                for (i in index..maxRange) {
                    aColorSpread[i] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

                    angle += difAngle
                    if (angle >= 360.0f) angle -= 360.0f

                    lum += difLum
                }
            } else {
                for (i in index..maxRange) {
                    aColorSpread[i] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

                    angle -= difAngle
                    if (angle < 0.0f) angle += 360.0f

                    lum += difLum
                }
            }

            index = maxRange + 1

            lum = lum2

        }

        for (cr in 0 until aColorDataList.size) {
            aRgbColorsList[cr] = aColorSpread[(aColorDataList[cr].range * mColorRangeLastIndex).toInt()]
        }
    }

    private fun processColorSpread() {
        if (aColorDataList.size < 2) return

        var col1: Int
        var col2: Int

        aColorSpread[0] = aColorDataList[0].color
        aColorSpread[mColorRangeLastIndex] = aColorDataList[aColorDataList.lastIndex].color

        for (cr in 1 until aColorDataList.size) {
            val lhs = (aColorDataList[cr- 1].range * mColorRangeLastIndex).toInt()
            val rhs = (aColorDataList[cr].range * mColorRangeLastIndex).toInt()
            val maxd = rhs - lhs

            col1 = aColorDataList[cr - 1].color
            col2 = aColorDataList[cr].color

            val r: IntArray = colorChannelRange(maxd, Color.red(col1), Color.red(col2))
            val g: IntArray = colorChannelRange(maxd, Color.green(col1), Color.green(col2))
            val b: IntArray = colorChannelRange(maxd, Color.blue(col1), Color.blue(col2))

            var n = 0
            for(i in lhs + 1..rhs) {
                aColorSpread[i] = Color.argb(255, r[n], g[n], b[n])
                n++
            }
        }

        for (cr in 0 until aColorDataList.size) {
            aRgbColorsList[cr] = aColorSpread[(aColorDataList[cr].range * mColorRangeLastIndex).toInt()]
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
