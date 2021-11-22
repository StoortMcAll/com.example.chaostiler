package com.fractal.tiler

// region Variable Declaration

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fractal.tiler.MainActivity.Companion.DataProcess
import com.fractal.tiler.MainActivity.Companion.mSeekbarMax
import com.fractal.tiler.MainActivity.Companion.rand
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import com.fractal.tiler.MainActivity.Companion.myResources
import kotlin.math.*

private const val mMaximumColorRangeSeekbarProgress = mSeekbarMax

data class DRgbDataItem(val range: Int = 0, val color: Int = 0)

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

private var colArray = IntArray(MainActivity.mSeekbarMax)

// endregion

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
            DRgbDataItem(0, Color.argb(255, 0, 0, 0)),
            DRgbDataItem(96, Color.argb(255, 96, 96, 96)),
            DRgbDataItem(160, Color.argb(255, 128, 128, 128)),
            DRgbDataItem(288, Color.argb(255, 224, 224, 224)),
            DRgbDataItem(511, Color.WHITE)))

        addColorRanges(listOf(
                 DRgbDataItem(0, Color.argb(255, 32, 0, 0)),
                 DRgbDataItem(32, Color.RED),
                 DRgbDataItem(96, Color.YELLOW),
                 DRgbDataItem(255, Color.YELLOW)))

        addColorRanges(listOf(
                DRgbDataItem(0, Color.argb(255, 0, 0, 32)),
                DRgbDataItem(32, Color.CYAN),
                DRgbDataItem(96, Color.RED),
                DRgbDataItem(255, Color.GREEN)))

        setCurrentColorRange(0)
    }

    fun getProgress(): Int {
        return aCurrentRange.getRangeProgress()
    }

    fun setProgress(prog: Int) {
        aCurrentRange.setRangeProgress(prog)

        setCurrentColorRange(aCurrentRange.mColorRangeID)

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

    fun addNewColorA() : ColorRangeClass {
        aCurrentRange = addNewRandomHSVRange()

        setCurrentColorRange(aCurrentRange.mColorRangeID)

        mNewColors = true

        return aCurrentRange
    }

    fun addNewColorB() : ColorRangeClass {
        aCurrentRange = addNewRandomPrimariesRange()

        setCurrentColorRange(aCurrentRange.mColorRangeID)

        mNewColors = true

        return aCurrentRange
    }

    private fun addNewRandomPrimariesRange() : ColorRangeClass{
        var dataitemcount = 3//rand.nextInt(1, 4)

        var range : Int
        var add : Int

        if (dataitemcount == 1) range = 256
        else if (dataitemcount == 2) range = 128
        else range = 64
        add = range// * 2

        var color : Int
        var colorIndex : Int
        var lastColorIndex : Int

        if (rand.nextBoolean()){
            color = Color.BLACK
            lastColorIndex = -1
        } else{
            lastColorIndex = rand.nextInt(0,  aPrimaries.colors.size)
            color = aPrimaries.colors[lastColorIndex]
        }

        val tempColorDataItemList = mutableListOf(DRgbDataItem(0, color))

        do{
            do {
                colorIndex = rand.nextInt(0, aPrimaries.colors.size)
            }while (colorIndex == lastColorIndex)

            tempColorDataItemList.add(
                DRgbDataItem(range, aPrimaries.colors[colorIndex]))

            range += add

            //add *= 2

            lastColorIndex = colorIndex

        }while(--dataitemcount > 0)

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addNewRandomColorsRange() : ColorRangeClass {
        var counter = rand.nextInt(1, 4)

        var range = 0

        var colorDataItem = DRgbDataItem(range,
            Color.argb(255,
                rand.nextInt(0, 32),
                rand.nextInt(0, 32),
                rand.nextInt(0, 32)))

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var mult = 1
        var mmax : Int
        do{
            val r1 = colorDataItem.color.red
            val g1 = colorDataItem.color.green
            val b1 = colorDataItem.color.blue

            val r2 = rand.nextInt(0, 256)
            val g2 = rand.nextInt(0, 256)
            val b2 = rand.nextInt(0, 256)

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

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addNewRandomHSVRange() : ColorRangeClass {
        var counter = rand.nextInt(2, 4)

        var range : Int
        var add : Int

        if (counter == 1) range = 256
        else if (counter == 2) range = 128
        else range = 64
        add = range// * 2

        var isDifPos: Boolean
        val angle = rand.nextInt(0, 256)

        var isLumHi = rand.nextInt(4) < 2
        var lum = if (isLumHi) 159 else 16

        var color = Color.argb(255, angle, 255, lum)

        var colorDataItem = DRgbDataItem(0, color)

        val tempColorDataItemList = mutableListOf(colorDataItem)


        var dif : Int
        do{
            dif = rand.nextInt(8, 24)
            isDifPos = rand.nextBoolean()

            isLumHi = if (isLumHi) rand.nextInt(4) < 2 else true
            lum = if (isLumHi) 159 else 16

            color = Color.argb(if (isDifPos) 128 else 0, dif, 255, lum)

            colorDataItem = DRgbDataItem(range, color)

            range += add

            //add *= 2

            tempColorDataItemList.add(colorDataItem)
        }while(--counter > 0)

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

class ColorRangeClass(id : Int, colorDataList: List<DRgbDataItem>, dataprocess : DataProcess, var isHSV: Boolean = false) {

    // region Variable Declaration

    var progressIncrement = mMaximumColorRangeSeekbarProgress
    var progressSecond = mMaximumColorRangeSeekbarProgress
    var progressStatistic = mMaximumColorRangeSeekbarProgress

    var mColorRangeID = id

    var dataProcess = dataprocess

    val mColorDataList = colorDataList

    val colorRangeBitmap : Bitmap = Bitmap.createBitmap(mSeekbarMax, 1, Bitmap.Config.ARGB_8888)
    lateinit var colorRangeDrawable : Drawable

    val mColorSpreadCount = colorDataList.last().range + 1

    var aColorSpread = IntArray(mColorSpreadCount)

    var primaryColors = IntArray(colorDataList.size)

    // endregion

    init {
        if (isHSV) processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
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

        updateColorSpreadBitmap()
    }


    fun updateColorSpreadBitmap() {
        if (dataProcess == DataProcess.LINEAR) {
            drawColorSpreadForIncremental(progressIncrement)
        } else {
            drawColorSpreadForSinwave(progressStatistic)
        }

        colorRangeDrawable = colorRangeBitmap.toDrawable(MainActivity.myResources)
    }


    private fun drawColorSpreadForIncremental(currentPos: Int = mSeekbarMax) {

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

        val colorscount = 32 + ((aColorSpread.lastIndex - 32) * seekPosAsFraction).toInt()

        val bmWid = colorRangeBitmap.width
        val wd = bmWid - 1
        val widthover1 = 1.0f / wd
        var value: Int

        for (x in 0..wd) {
            value = (colorscount * (x * widthover1)).toInt()

            colArray[x] = aColorSpread[value]
        }

        colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
    }

    private fun drawColorSpreadForSinwave(currentPos: Int) {

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

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

            colArray[x] = aColorSpread[index]
        }

        colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
    }

    private fun drawColorSpreadForStatistical(currentPos: Int, pixelDataCopy: PixelData) {
        val colorscount = aColorSpread.lastIndex

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

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

            colArray[x] = aColorSpread[basecol]
        }
        colArray[wd] = aColorSpread[colorscount]

        colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
    }

    private fun drawColorSpreadForCosec(currentPos: Int, pixelDataCopy: PixelData) {

        val colorscount = aColorSpread.lastIndex

        val maxhits = pixelDataCopy.mMaxHits

        val seekPosAsFraction = currentPos * (1.0 / mSeekbarMax.toDouble())

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

            colArray[x] = aColorSpread[basecol]
        }
        colArray[wd] = aColorSpread[colorscount]

        colorRangeBitmap.setPixels(colArray, 0, bmWid, 0, 0, bmWid, 1)
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

            maxRange = mColorDataList[cr].range - mColorDataList[cr - 1].range

            isDifPos = colorData.alpha == 128
            difAngle = (colorData.red * bin2Rad) / maxRange

            lum2 = colorData.blue / 255.0f
            difLum = lum2 - lum
            if (difLum != 0.0F) difLum /= maxRange

            if (isDifPos) {
                for (i in 0 until maxRange) {
                    aColorSpread[index++] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

                    angle += difAngle
                    if (angle >= 360.0f) angle -= 360.0f

                    lum += difLum
                }
            } else {
                for (i in 0 until maxRange) {
                    aColorSpread[index++] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

                    angle -= difAngle
                    if (angle < 0.0f) angle += 360.0f

                    lum += difLum
                }
            }

            lum = lum2

        }

        aColorSpread[index] = Color.HSVToColor(floatArrayOf(angle, 1.0f, lum))

        for (cr in 0 until mColorDataList.size) {
            primaryColors[cr] = aColorSpread[mColorDataList[cr].range]
        }
    }

    private fun processColorSpread() {
        if (mColorDataList.size < 2) return

        var index = 1

        var col1: Int
        var col2: Int

        aColorSpread[0] = mColorDataList[0].color
        aColorSpread[aColorSpread.lastIndex] = mColorDataList[mColorDataList.lastIndex].color

        for (cr in 0 until mColorDataList.size) {
            primaryColors[cr] = mColorDataList[cr].color
        }

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
