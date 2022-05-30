package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fractal.tiler.MainActivity.Companion.animColorSpread
import com.fractal.tiler.MainActivity.Companion.mColorRangeLastIndex
import com.fractal.tiler.MainActivity.Companion.rand
import com.fractal.tiler.MainActivity.Companion.myResources

private const val mMaximumColorRangeSeekbarProgress = mColorRangeLastIndex

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

data class RangeColorDataItem(var range : Float, var color : Int){

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

    private var mColorRangeList = mutableListOf<ColorRangeClass>()

    private val colorBlendBitmap : Bitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    private val colorBlendColors = IntArray(mColorRangeLastIndex + 1)

    var mNewColors = true

    var mBlendingColors = false
    // endregion

    init {
        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.BLACK),
            RangeColorDataItem(0.5F, Color.GRAY),
            RangeColorDataItem(1.0F, Color.WHITE)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.WHITE),
            RangeColorDataItem(0.5F, Color.GRAY),
            RangeColorDataItem(1.0F, Color.BLACK)))

        addColorRanges(listOf(
                 RangeColorDataItem(0.0F, Color.YELLOW),
                 RangeColorDataItem(0.15F, Color.RED),
                 RangeColorDataItem(0.5F, Color.BLACK),
                 RangeColorDataItem(0.85F, Color.RED),
                RangeColorDataItem(1.0F, Color.YELLOW)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.BLACK),
            RangeColorDataItem(0.35F, Color.RED),
            RangeColorDataItem(0.5F, Color.YELLOW),
            RangeColorDataItem(0.65F, Color.RED),
            RangeColorDataItem(1.0F, Color.BLACK)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.BLACK),
            RangeColorDataItem(0.35F, Color.WHITE),
            RangeColorDataItem(0.5F, Color.BLACK),
            RangeColorDataItem(0.65F, Color.WHITE),
            //RangeColorDataItem(0.75F, Color.argb(255, 255, 119, 0)),
            RangeColorDataItem(1.0F, Color.BLACK)))

        addColorRanges(listOf(
            RangeColorDataItem(0.0F, Color.WHITE),
            RangeColorDataItem(0.35F, Color.BLACK),
            RangeColorDataItem(0.5F, Color.WHITE),
            RangeColorDataItem(0.65F, Color.BLACK),
            //RangeColorDataItem(0.75F, Color.argb(255, 255, 119, 0)),
            RangeColorDataItem(1.0F, Color.WHITE)))

/*

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
        */

        setCurrentColorRange(0)
    }

    fun getColorRangeFromIndex(index : Int) : ColorRangeClass {
        val correctedIndex : Int

        if (index < 0)
            correctedIndex = mColorRangeList.lastIndex
        else if (index > mColorRangeList.lastIndex)
            correctedIndex = 0
        else
            correctedIndex = index

        return mColorRangeList[correctedIndex]
    }

    /**
     * Set the position of ActiveColor n, between the Color n-1 and Color n+1, in the CurrentColorRange
     * **/
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

    fun blendColorRanges(colorRangeFrom : IntArray, colorRangeTo : IntArray,
                         percentage : Float, copyToAnimRange : Boolean = false) : IntArray{
        var colorFrom : Int
        var colorTo : Int

        mBlendingColors = true

        when (percentage) {
            0.0f -> {
                colorRangeFrom.copyInto(colorBlendColors, 0, 0, mColorRangeLastIndex + 1)
            }
            1.0f -> {
                colorRangeTo.copyInto(colorBlendColors, 0, 0, mColorRangeLastIndex + 1)
            }
            else -> {
                for (c in 0..mColorRangeLastIndex) {
                    colorFrom = colorRangeFrom[c]
                    colorTo = colorRangeTo[c]

                    colorBlendColors[c] = Color.argb(
                        255,
                        colorFrom.red + ((colorTo.red - colorFrom.red) * percentage).toInt(),
                        colorFrom.green + ((colorTo.green - colorFrom.green) * percentage).toInt(),
                        colorFrom.blue + ((colorTo.blue - colorFrom.blue) * percentage).toInt()
                    )
                }
            }
        }

        if (copyToAnimRange) animColorSpread = colorBlendColors.copyOf()

        mBlendingColors = false

        return colorBlendColors
    }

    /**
    Add new HSV colorRange
     */
    fun addNewColorA() : ColorRangeClass {
        aCurrentRange = addFullHsvRandomRange()

        setCurrentColorRange(aCurrentRange.mColorRangeID)

        mNewColors = true

        return aCurrentRange
    }
    /**
    Add new RGB colorRange
     */
    fun addNewColorB() : ColorRangeClass {
        aCurrentRange = addNewRandomPrimariesRange3()

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
                colorIndex = rand.nextInt(1, mPrimaryColourCount)
            }while (colorIndex == lastColorIndex || (lastColorIndex > 12 && colorIndex > 12))

            tempColorDataItemList.add(
                RangeColorDataItem(i * add, aPrimaries.colors[colorIndex]))

            lastColorIndex = colorIndex
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }


    private fun addNewRandomPrimariesRange2() : ColorRangeClass{
        val dataitemcount = rand.nextInt(2, 5)

        val add = 1.0F / dataitemcount

        var color : Int
        var colorHue  = 0.0f

        val tempColorDataItemList = mutableListOf<RangeColorDataItem>()

        for (i in 0..dataitemcount){
            if(i == 0) {
                colorHue = rand.nextInt(0, 360).toFloat()

                color = Color.HSVToColor(floatArrayOf(colorHue, 1.0f, rand.nextFloat()))
            } else {
                colorHue += 60 + rand.nextInt(0, 240)
                if(colorHue > 359) colorHue -= 360

                color = Color.HSVToColor(floatArrayOf(colorHue, 1.0f, (0.5f + rand.nextFloat()) * 0.5f))
            }

            tempColorDataItemList.add(
                RangeColorDataItem(i * add, color))
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }


    private fun addNewRandomPrimariesRange3() : ColorRangeClass{
        val dataitemcount = rand.nextInt(2, 4)

        val add = 1.0F / dataitemcount

        var color : Int
        var colorHue  = 0.0f

        var hueAdd = 45.0f + 45.0f * (2.0f / dataitemcount)
        val huePos = rand.nextBoolean()
        if (!huePos) hueAdd = -hueAdd

        var satMax = rand.nextBoolean()
        var satVal = if (satMax) 1.0f else 0.5f
        var value = if (rand.nextBoolean()) 0.0f else 1.0f

        val tempColorDataItemList = mutableListOf<RangeColorDataItem>()

        for (i in 0..dataitemcount){
            if(i == 0) {
                colorHue = rand.nextInt(0, 359).toFloat()

                color = Color.HSVToColor(floatArrayOf(colorHue, satVal, value))
            } else {
                colorHue += hueAdd
                if (huePos) {
                    if (colorHue > 359) colorHue -= 360
                } else if(colorHue < 0) colorHue += 360

                if (satMax){
                    if (rand.nextBoolean()){
                        satMax = false
                        satVal = rand.nextFloat() * 0.5f
                    }
                } else{
                    satMax = true
                    satVal = 1.0f
                }

                if (value < 1.0f){
                    value = 1.0f
                } else{
                    if (rand.nextBoolean()){
                        value = rand.nextFloat() * 0.5f
                    }
                }

                color = Color.HSVToColor(floatArrayOf(colorHue, satVal, value))
            }

            tempColorDataItemList.add(
                RangeColorDataItem(i * add, color))
        }

        addColorRanges(tempColorDataItemList)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }

    private fun addFullHsvRandomRange() : ColorRangeClass {
        val dataitemcount = rand.nextInt(2, 4)

        var lastColor = getNewHsvColor(IntArray(1), true)

        var colorDataItem = RangeColorDataItem(
            0.0F,
            Color.argb(255, lastColor[0], lastColor[1], lastColor[2]))

        val tempColorDataItemList = mutableListOf(colorDataItem)

        var nextColor : IntArray

        val add = 1.0F / dataitemcount

        for (i in 1..dataitemcount){
            nextColor = getNewHsvColor(lastColor)

            colorDataItem = RangeColorDataItem(
                i * add,
                Color.argb(255, nextColor[0], nextColor[1], nextColor[2]))

            tempColorDataItemList.add(colorDataItem)

            lastColor = nextColor.copyOf()
        }

        addColorRanges(tempColorDataItemList, true)

        setCurrentColorRange(mColorRangeList[mColorRangeList.lastIndex].mColorRangeID)

        return aCurrentRange
    }
    private fun getNewHsvColor(lastColor : IntArray, isFirst : Boolean = false) : IntArray{
        var h : Float
        val s : Float
        val v : Float

        if (isFirst) {
            h = rand.nextFloat()
            s = if (rand.nextInt(16) < 12) 1.0f else 0.0f
            v = if (rand.nextInt(8) < 6) 1.0f else 0.0f
        } else {
            h = (lastColor[0] / 255.0f) +
                    ((rand.nextFloat() * 0.25f + 0.125f) *
                        if (rand.nextBoolean()) 1.0f else -1.0f)

            if (h < 0.0f) h += 1.0f
            else if ( h >= 1.0f) h -= 1.0f

            s = if (lastColor[1] / 255.0f > 0.5f && rand.nextInt(8) > 5){
                rand.nextFloat() * 0.25f
            } else 0.75f + rand.nextFloat() * 0.25f

            v = if (lastColor[2] / 255.0f > 0.5f && rand.nextInt(8) > 5){
                rand.nextFloat() * 0.25f
            } else 0.75f + rand.nextFloat() * 0.25f
        }

        return intArrayOf((h * 255).toInt(),(s * 255).toInt(), (v * 255).toInt())
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

    val threeSixtyover255 = 360.0f / 255.0f
    val oneOver255 = 1.0f / 255.0f
    var mColorRangeID = id

    var mActiveColorIndex = 1

    val colorRangeBitmap: Bitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)
    var colorButtonBitmap: Bitmap = Bitmap.createBitmap(mColorRangeLastIndex + 1, 1, Bitmap.Config.ARGB_8888)

    var colorRangeDrawable = RoundedBitmapDrawableFactory.create(myResources, colorRangeBitmap)
    lateinit var colorButtonDrawable : RoundedBitmapDrawable

    var aColorSpread = IntArray(mColorRangeLastIndex + 1)

    val aRgbColorsList = IntArray(aColorDataList.size)

    var lhsColorProgress = 0
    var rhsColorProgress = 100
    var seekbarMax = 100

    // endregion

    init {
        calcProgress()

        if (isHSV) processNewHSV()//processHSVSpread()
        else processColorSpread()

        updateColorSpreadBitmap()
    }

    fun copyColors() : IntArray {
        return aColorSpread.copyOf()
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

        if (isHSV) processNewHSV()
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

    private fun processNewHSV() {
        if (aColorDataList.size < 2) return

        var index = 0

        var startRange : Int
        var endRange = 0

        var steps : Int

        var oldHsv: FloatArray
        var newHsv: FloatArray

        var hDif : Float
        var newH : Float
        var sDif : Float
        var newS : Float
        var vDif : Float
        var newV : Float

        oldHsv = myRgbToHsv(aColorDataList[0].color)

        aColorSpread[index++] = Color.HSVToColor(oldHsv)

        for (cr in 1 until aColorDataList.size) {
            startRange = endRange + 1

            endRange = (aColorDataList[cr].range * mColorRangeLastIndex).toInt()

            steps =  endRange - startRange

            newHsv = myRgbToHsv(aColorDataList[cr].color)

            hDif = newHsv[0] - oldHsv[0]

            if (hDif > 0){
                if (hDif > 180) hDif -= 360
            } else {
                if (hDif < -180) hDif += 360
            }

            hDif /= steps

            sDif = (newHsv[1] - oldHsv[1]) / steps

            vDif = (newHsv[2] - oldHsv[2]) / steps

            for (i in 0..steps){
                newH = oldHsv[0] + (hDif * i)
                if (newH < 0.0f) newH += 360.0f
                else if (newH >= 360.0f) newH -= 360.0f

                newS = oldHsv[1] + (sDif * i)
                newV = oldHsv[2] + (vDif * i)

                aColorSpread[index++] = Color.HSVToColor(255, floatArrayOf(newH, newS, newV))
            }

            oldHsv = newHsv.copyOf()
        }

        for (cr in 0 until aColorDataList.size) {
            aRgbColorsList[cr] = aColorSpread[(aColorDataList[cr].range * mColorRangeLastIndex).toInt()]
        }
    }
    private fun myRgbToHsv(color : Int) : FloatArray {
        val dhsv = FloatArray(3)

        dhsv [0] = color.red * threeSixtyover255

        dhsv [1] = color.green * oneOver255

        dhsv [2] = color.blue * oneOver255

        return dhsv
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
