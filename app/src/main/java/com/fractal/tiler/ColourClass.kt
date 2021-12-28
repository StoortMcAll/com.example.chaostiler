package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.mColorRangeLastIndex
import com.fractal.tiler.MainActivity.Companion.rand
import com.fractal.tiler.MainActivity.Companion.myResources
import kotlin.math.*

private const val mMaximumColorRangeSeekbarProgress = mColorRangeLastIndex

data class DRgbDataItem(var range : Float, var color : Int){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DRgbDataItem

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

//private var colArray = IntArray(mColorRangeLastIndex + 1)

// endregion

/**
 * Handles all colorRanges
 */
class ColorClass {

    // region Variable Declaration

    private val mPrimaryColourCount = 24

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
            Color.argb(255, 32,12, 96),

            Color.argb(255, 48,16, 0),
            Color.argb(255, 16,0, 48),
            Color.argb(255, 0,48, 16),
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
            DRgbDataItem(0.0F, Color.argb(255, 0, 0, 0)),
            DRgbDataItem(0.2F, Color.argb(255, 96, 96, 96)),
            DRgbDataItem(0.33F, Color.argb(255, 128, 128, 128)),
            DRgbDataItem(0.56F, Color.argb(255, 224, 224, 224)),
            DRgbDataItem(1.0F, Color.WHITE)))

        addColorRanges(listOf(
                 DRgbDataItem(0.0F, Color.argb(255, 32, 0, 0)),
                 DRgbDataItem(0.13F, Color.RED),
                 DRgbDataItem(0.38F, Color.YELLOW),
                 DRgbDataItem(1.0F, Color.YELLOW)))

        addColorRanges(listOf(
                DRgbDataItem(0.0F, Color.argb(255, 0, 0, 32)),
                DRgbDataItem(0.13F, Color.CYAN),
                DRgbDataItem(0.38F, Color.RED),
                DRgbDataItem(1.0F, Color.GREEN)))

        setCurrentColorRange(0)
    }

    fun getProgress(): Int {
        return aCurrentRange.progressIncrement
    }

    fun setProgress(prog: Int) {
        aCurrentRange.setRangeProgress(prog)

        //setCurrentColorRange(aCurrentRange.mColorRangeID)

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

    fun getPaletteByID(colorRangeID : Int) : ColorRangeClass {
        if (colorRangeID < mColorRangeList.size){
            return mColorRangeList[colorRangeID]
        }

        return mColorRangeList[0]
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
        var dataitemcount = rand.nextInt(2, 5)

        val itemsSlicesCount = Math.pow(2.0, dataitemcount.toDouble()).toInt() - 1
        //for (i in 0 until dataitemcount){
         //   itemsSlicesCount += Math.pow(2.0, i.toDouble()).toInt()
        //}

        val add = 1.0F / dataitemcount

        val color : Int
        var colorIndex : Int
        var lastColorIndex : Int

        if (rand.nextBoolean()){
            color = Color.BLACK
            lastColorIndex = -1
        } else{
            lastColorIndex = rand.nextInt(0,  aPrimaries.colors.size)
            color = aPrimaries.colors[lastColorIndex]
        }

        var value = 1.0f / itemsSlicesCount
        val testArray = FloatArray(dataitemcount + 1){0.0f}
        //for (i in 1..dataitemcount){
         //   testArray[i] = value
            //value += (Math.pow(2.0, i.toDouble()) / itemsSlicesCount).toFloat()
        //}
//todo rescale colorRanges

        val tempColorDataItemList = mutableListOf(DRgbDataItem(0.0F, color))
        for (i in 1..dataitemcount){
            do {
                colorIndex = rand.nextInt(0, aPrimaries.colors.size)
            }while (colorIndex == lastColorIndex)

            tempColorDataItemList.add(
                DRgbDataItem(value, aPrimaries.colors[colorIndex]))
                //DRgbDataItem(i * add, aPrimaries.colors[colorIndex]))
            testArray[i] = value
            lastColorIndex = colorIndex
            value += (Math.pow(2.0, i.toDouble()) / itemsSlicesCount).toFloat()
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addNewRandomColorsRange() : ColorRangeClass {
        var dataitemcount = rand.nextInt(2, 5)

        val add = 1.0F / dataitemcount

        var colorDataItem = DRgbDataItem(0.0F,
            Color.argb(255,
                rand.nextInt(0, 256),
                rand.nextInt(0, 256),
                rand.nextInt(0, 256)))

        val tempColorDataItemList = mutableListOf(colorDataItem)

        for (i in 1..dataitemcount){
            val r1 = colorDataItem.color.red
            val g1 = colorDataItem.color.green
            val b1 = colorDataItem.color.blue

            val r2 = rand.nextInt(0, 256)
            val g2 = rand.nextInt(0, 256)
            val b2 = rand.nextInt(0, 256)

            val color = Color.argb(255, r2, g2, b2)

            colorDataItem = DRgbDataItem(i * add, color)

            tempColorDataItemList.add(colorDataItem)
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addNewRandomHSVRange() : ColorRangeClass {
        var dataitemcount = rand.nextInt(2, 5)

        val add = 1.0F / dataitemcount

        var isDifPos: Boolean
        val angle = rand.nextInt(0, 256)

        var isLumHi = rand.nextInt(4) < 2
        var lum = if (isLumHi) 159 else 16

        var color = Color.argb(255, angle, 255, lum)

        var colorDataItem = DRgbDataItem(0.0F, color)

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var dif : Int
        for (i in 1..dataitemcount){
            dif = rand.nextInt(8, 24)
            isDifPos = rand.nextBoolean()

            isLumHi = if (isLumHi) rand.nextInt(4) < 2 else true
            lum = if (isLumHi) 159 else 16

            color = Color.argb(if (isDifPos) 128 else 0, dif, 255, lum)

            colorDataItem = DRgbDataItem(i * add, color)

            tempColorDataItemList.add(colorDataItem)
        }

        addColorRanges(tempColorDataItemList, DataProcess.LINEAR,true)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun df(d1 : Int, d2 : Int) : Int{
        return abs(d1 - d2)
    }

    private fun addColorRanges(colorRanges: List<DRgbDataItem>, dataprocess : DataProcess = DataProcess.LINEAR, isHSV : Boolean = false){
        mColorRangeList.add(ColorRangeClass(mRangeIndexCounter++, colorRanges, dataprocess, isHSV))
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
class ColorRangeClass(id : Int, colorDataList: List<DRgbDataItem>, dataprocess : DataProcess, var isHSV: Boolean = false) {

    // region Variable Declaration

    var progressIncrement = mMaximumColorRangeSeekbarProgress
    var progressStatistic = mMaximumColorRangeSeekbarProgress

    var mColorRangeID = id

    var mActiveColorButtonId = 1

    var dataProcess = dataprocess

    val mColorDataList = colorDataList

    val colorRangeBitmap : Bitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    var colorRangeDrawable = colorRangeBitmap.toDrawable(myResources)

    var activeButtonBitmap : Bitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    var activeButtonDrawable = activeButtonBitmap.toDrawable(myResources)

    var aColorSpread = IntArray(mColorRangeLastIndex + 1)

    var aDataListColors = IntArray(colorDataList.size)

    var lhsColorIndex = 0
    var rhsColorIndex = 100
    var seekbarMax = 100

    // endregion

    init {
        calcProgress()

        if (isHSV) processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
    }


    fun setActiveColorId(colorId : Int){
        val oldId = mActiveColorButtonId
        mActiveColorButtonId = colorId

        calcProgress()

        if (oldId != colorId) updateSeekbarBitmap()
    }

    fun calcProgress() : Int{
        lhsColorIndex = (mColorRangeLastIndex * mColorDataList[mActiveColorButtonId - 1].range).toInt()
        progressIncrement = (mColorRangeLastIndex * mColorDataList[mActiveColorButtonId].range).toInt() - lhsColorIndex
        rhsColorIndex = (mColorRangeLastIndex * mColorDataList[mActiveColorButtonId + 1].range).toInt()
        seekbarMax = rhsColorIndex - lhsColorIndex

        if (progressIncrement == 0) progressIncrement = 1
        else if (progressIncrement == seekbarMax) progressIncrement = seekbarMax - 1

        return progressIncrement
    }

    fun setRangeProgress(prog : Int){
        //if (dataProcess == MainActivity.Companion.DataProcess.LINEAR){

        progressIncrement = prog
        if (progressIncrement == 0) progressIncrement = 1
        else if (progressIncrement == seekbarMax) progressIncrement = seekbarMax - 1
      //  progressSecond = progressIncrement

        mColorDataList[mActiveColorButtonId].range =
            (lhsColorIndex + progressIncrement) * (1.0F / mColorRangeLastIndex.toFloat())

        if (isHSV) processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
    }

    fun updateColorSpreadBitmap() {
        updateSeekbarBitmap()

        updateRangeBitmap()
    }

    private fun updateSeekbarBitmap() {
        activeButtonBitmap = Bitmap.createBitmap(aColorSpread,  lhsColorIndex, mColorRangeLastIndex + 1, seekbarMax, 1, Bitmap.Config.ARGB_8888)
        activeButtonDrawable = activeButtonBitmap.toDrawable(myResources)
    }
    private fun updateRangeBitmap() {
        colorRangeBitmap.setPixels(aColorSpread, 0, colorRangeBitmap.width, 0, 0, colorRangeBitmap.width, 1)
        colorRangeDrawable = colorRangeBitmap.toDrawable(myResources)
    }

    private fun drawColorSpreadForSinwave(currentPos: Int) {

        val seekPosAsFraction = currentPos * (1.0 / mColorRangeLastIndex.toDouble())

        val colorscount = aColorSpread.lastIndex

        val arc = PI / 2.0
        val bmWid = colorRangeBitmap.width
        val wd = bmWid - 1
        val widthover1 = 1.0f / wd
        var valueInc: Float
        var valueArc: Float
        var dif: Float
        var index: Int

        for (x in 0..wd) {
            valueInc = x * widthover1
            valueArc = sin(valueInc * arc).toFloat()

            dif = valueArc - valueInc

            index = ((valueInc + dif * seekPosAsFraction) * colorscount).toInt()

            //colArray[x] = aColorSpread[index]
        }

        //colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
    }
    private fun drawColorSpreadForCosec(currentPos: Int, pixelDataCopy: PixelData) {

        val colorscount = aColorSpread.lastIndex

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / mColorRangeLastIndex.toDouble())

        val percentage = FloatArray(pixelDataCopy.mMaxHits + 1) { 0.0F }
        for (i in 1 until pixelDataCopy.mMaxHits) {
            percentage[i] =
                percentage[i - 1] + (pixelDataCopy.aHitStats[i - 1] / pixelDataCopy.arraySize.toFloat())
        }
        percentage[pixelDataCopy.mMaxHits] = 1.0F

        val bmWid = colorRangeBitmap.width
        val wd = bmWid - 1
        val dx = 1.0f / wd
        var value: Int
        var findex: Float
        var basecol: Int
        var df: Int
        var fpos: Float

        for (x in 0 until wd) {
            fpos = maxhits * (x * dx)

            basecol = ((x * dx) * colorscount).toInt()

            value = fpos.toInt()

            findex = percentage[value]

            fpos -= value

            findex += (percentage[value + 1] - percentage[value]) * fpos

            value = (colorscount * findex).toInt()

            df = value - basecol

            basecol += (df * seekPosAsFraction).toInt()

            //colArray[x] = aColorSpread[basecol]
        }
        //colArray[wd] = aColorSpread[colorscount]

        //colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
    }
    private fun setCosecValues(width: Int, colorsCount: Int): IntArray {
        val max = acos(1.0 / (width + 1.0))

        val slice = max / width

        val mult = colorsCount / width.toDouble()

        val colorIndex = IntArray(width + 1)

        for (x in 0..width) {
            colorIndex[x] = (mult * ((1.0 / cos(x * slice)) - 1.0)).toInt()
        }

        return colorIndex
    }

    private fun processHSVSpread() {
        if (mColorDataList.size < 2) return

        var isDifPos: Boolean
        var maxRange: Int
        var difAngle : Float
        var difLum : Float
        var lum2: Float
        var colorData : Int

        var index = 0
        val bin2Rad = 360.0f / 255.0f

        var angle = mColorDataList[0].color.red * bin2Rad
        var lum = mColorDataList[0].color.blue / 255.0f

        for (cr in 1 until mColorDataList.size) {
            colorData = mColorDataList[cr].color

            maxRange = (mColorDataList[cr].range * mColorRangeLastIndex).toInt()

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

        //aColorSpread[index] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

        for (cr in 0 until mColorDataList.size) {
            aDataListColors[cr] = aColorSpread[(mColorDataList[cr].range * mColorRangeLastIndex).toInt()]
        }
    }

    private fun processColorSpread() {
        if (mColorDataList.size < 2) return

        var index = 1

        var col1: Int
        var col2: Int

        aColorSpread[0] = mColorDataList[0].color
        aColorSpread[mColorRangeLastIndex] = mColorDataList[mColorDataList.lastIndex].color
        aDataListColors[0] = aColorSpread[0]

        for (cr in 1 until mColorDataList.size) {
            aDataListColors[cr] = mColorDataList[cr].color

            val lhs = (mColorDataList[cr- 1].range * mColorRangeLastIndex).toInt()
            val rhs = (mColorDataList[cr].range * mColorRangeLastIndex).toInt()
            val maxd = rhs - lhs

            col1 = mColorDataList[cr - 1].color
            col2 = mColorDataList[cr].color

            val r: IntArray = colorChannelRange(maxd, Color.red(col1), Color.red(col2))
            val g: IntArray = colorChannelRange(maxd, Color.green(col1), Color.green(col2))
            val b: IntArray = colorChannelRange(maxd, Color.blue(col1), Color.blue(col2))

            var n = 0
            for(i in lhs + 1..rhs) {
                aColorSpread[i] = Color.argb(255, r[n], g[n], b[n])
                n++
            }
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
