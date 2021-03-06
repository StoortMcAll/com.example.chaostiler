package com.fractal.tiler

// region Variable Declaration

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.minus
import com.fractal.tiler.MainActivity.Companion.bmImage
import com.fractal.tiler.MainActivity.Companion.clickPos
import com.fractal.tiler.MainActivity.Companion.mScaleFactor
import com.fractal.tiler.MainActivity.Companion.offset
import com.fractal.tiler.MainActivity.Companion.quiltType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat

// endregion


class MyImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    // region Variable Declaration

    //private var mScaleFitToCanvas = 1.0f

    private var paint = Paint()
    private lateinit var shader: Shader
    private val shaderMatrix = Matrix()

    private var paintGreyed = Paint()
    private var shaderGreyed: Shader
    private val textureGreyed = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    private var pIndex0 : Int = 0
    private var pIndex1 : Int = 0

    private var clickPos1 = PointF()
    private var texCoord = PointF()

    private var isSingleTouch = false

    private var mScaleDetector : ScaleGestureDetector

    // endregion

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var msc = mScaleFactor * detector.scaleFactor

            if (msc < 0.25f){
                msc = 0.25f
            }
            else if (msc > 6.0f) {
                msc = 6.0f
            }

            mScaleFactor = msc

            return true
        }
    }


    init {
        this.isClickable = false

        mScaleDetector = ScaleGestureDetector(context, scaleListener)

        bmImage = Bitmap.createBitmap(MainActivity.width, MainActivity.height, Bitmap.Config.ARGB_8888)

        textureGreyed.setPixel(0, 0, Color.argb(96, 33, 66, 99))
        shaderGreyed = BitmapShader(textureGreyed, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        paintGreyed.shader = shaderGreyed

        setBitmap(bmImage)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isSingleTouch = true

                pIndex0 = motionEvent.findPointerIndex(0)

                if (pIndex0 == -1) {
                    pIndex0 = motionEvent.findPointerIndex(1)
                }

                if (pIndex0 == -1) {
                    return true
                }

                motionEvent.getX(pIndex0).also { clickPos.x = it }
                motionEvent.getY(pIndex0).also { clickPos.y = it }

                texCoord = winPosToTextureCoord(PointF(clickPos.x, clickPos.y))

                offset = calcOffset(PointF(clickPos.x, clickPos.y), PointF(texCoord.x, texCoord.y))
            }
            MotionEvent.ACTION_MOVE -> {
                if (motionEvent.pointerCount > 1) {
                    if (isSingleTouch) {
                        pIndex0 = motionEvent.findPointerIndex(0)
                        pIndex1 = motionEvent.findPointerIndex(1)
                    }

                    clickPos.x = motionEvent.getX(pIndex0)
                    clickPos.y = motionEvent.getY(pIndex0)

                    clickPos1.x = motionEvent.getX(pIndex1)
                    clickPos1.y = motionEvent.getY(pIndex1)

                    clickPos = findTouchCenter(PointF(clickPos.x, clickPos.y), PointF(clickPos1.x, clickPos1.y))

                    if (isSingleTouch) {
                        isSingleTouch = false

                        texCoord = winPosToTextureCoord(PointF(clickPos.x, clickPos.y))
                    }

                    offset = calcOffset(PointF(clickPos.x, clickPos.y), PointF(texCoord.x, texCoord.y))
                }
                else {
                    if (!isSingleTouch) {
                        isSingleTouch = true

                        pIndex0 = if (motionEvent.findPointerIndex(0) == -1)
                            motionEvent.findPointerIndex(1)
                        else
                            motionEvent.findPointerIndex(0)

                        clickPos.x = motionEvent.getX(pIndex0)
                        clickPos.y = motionEvent.getY(pIndex0)

                        texCoord = winPosToTextureCoord(PointF(clickPos.x, clickPos.y))
                    } else {
                        clickPos.x = motionEvent.getX(pIndex0)
                        clickPos.y = motionEvent.getY(pIndex0)
                    }

                    offset = calcOffset(PointF(clickPos.x, clickPos.y), PointF(texCoord.x, texCoord.y))
                }
            }
        }

        mScaleDetector.onTouchEvent(motionEvent)

        invalidate()

        return true
    }


    fun setBitmap(bitmap : Bitmap){
        bmImage = bitmap

        shader = BitmapShader(bmImage, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

        setOffsetScale()

        paint.shader = shader

        invalidate()
    }

    fun offsetForFullscreen(toFullscreen : Boolean){
        val textpos : PointF

        if(toFullscreen) {
            textpos = winPosToTextureCoord(PointF(0.0f,0.0f))

            offset = calcOffset(PointF(0.0f,height / 2.0f), textpos)
        }else{
            textpos = winPosToTextureCoord(PointF(0.0f,height / 2.0f))

            offset = calcOffset(PointF(0.0f,0.0f), textpos)
        }

        setOffsetScale()
    }

    private fun setOffsetScale(){
        shaderMatrix.setTranslate(offset.x, offset.y)

        shaderMatrix.preScale(mScaleFactor, mScaleFactor)

        shader.setLocalMatrix(shaderMatrix)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
       // mScaleFitToCanvas = this.width / bmImage.width.toFloat()

        setOffsetScale()

        canvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)

    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun paintWallpaper(isWallpaper : Boolean)
    {
        if (isWallpaper) {
            val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(this.context)
            if (wallpaperManager.isWallpaperSupported) {

                val wd: Int
                val ht: Int

                if (isWallpaper && Build.VERSION.SDK_INT == 30) {
                    wd = (width * 1.1).toInt()
                    ht = (height * 1.1).toInt()

                    val tempOffset = PointF(offset.x + (width * 0.05f), offset.y + (height * 0.05f))
                    val tempScale = mScaleFactor
                    shaderMatrix.setTranslate(tempOffset.x, tempOffset.y)
                    shaderMatrix.preScale(tempScale, tempScale)
                    shader.setLocalMatrix(shaderMatrix)

                } else {
                    wd = width
                    ht = height
                }

                val texture = Bitmap.createBitmap(wd, ht, Bitmap.Config.ARGB_8888)

                val canvas = Canvas(texture)
                canvas.drawRect(0.0f, 0.0f, wd.toFloat() - 1.0f, ht.toFloat() - 1.0f, paint)

                try {
                    if (Build.VERSION.SDK_INT == 30)
                        wallpaperManager.setBitmap(texture, null, false)
                    else
                        wallpaperManager.setBitmap(texture)

                } catch (ex: IOException) {
                }


            }
        }else {
            bitmapToFile(bmImage)
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File? {
        var file: File? = null

        val filename = resources.getStringArray(R.array.formula_types)[quiltType.ordinal] + "-"

        val filetype = ".png"

        val pattern = "0000"
        val formatter = DecimalFormat(pattern)

        val filepath = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_PICTURES + File.separator
        var filenumber = 0
        var filefullname: String

        do {
            val filenumberText = formatter.format(filenumber++)

            filefullname = filepath + filename + filenumberText + filetype
        }while(File(filefullname).exists())


        return try {
            file = File(filefullname)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    private fun winPosToTextureCoord(winLocation: PointF) : PointF{
        val textureCoord = winLocation - offset

        if ( textureCoord.x < 0.0f) {
            textureCoord.x += bmImage.width * mScaleFactor
        }
        if ( textureCoord.y < 0.0f) {
            textureCoord.y += bmImage.height * mScaleFactor
        }

        textureCoord.x %= bmImage.width * mScaleFactor
        textureCoord.y %= bmImage.height * mScaleFactor

        textureCoord.x /= mScaleFactor
        textureCoord.y /= mScaleFactor

        return textureCoord
    }

    private fun calcOffset(winLocation: PointF, textureCoord: PointF) : PointF{
        winLocation.x -= textureCoord.x * mScaleFactor

        if (winLocation.x < 0)
            winLocation.x += bmImage.width * mScaleFactor

        winLocation.y -= textureCoord.y * mScaleFactor

        if (winLocation.y < 0)
            winLocation.y += bmImage.height * mScaleFactor

        return winLocation
    }

    private fun findTouchCenter(p0: PointF, p1: PointF): PointF {
        val p3 = p0 - p1

        return PointF(p0.x + p3.x / 2.0f, p1.y + p3.y / 2.0f)
    }
}