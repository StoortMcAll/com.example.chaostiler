package com.fractal.tiler

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    // region Variable Declaration

    private val iconocatURL = "https://sites.google.com/view/wwwiconocatuk"
    private val privacyURL = "/privacy-policy"

    companion object{
        enum class QuiltType {SQUARE, SCRATCH, HEXAGONAL}

        enum class DataProcess {LINEAR, STATISTICAL}

        enum class ImageFilter {Blur, Gaussian, Motion, BoxBlur, Median}

        var filter = ImageFilter.Blur

        var quiltType = QuiltType.SQUARE

        var rand  = Random(0)

        const val width = 768; const val height = 768

        const val mColorRangeLastIndex = 511

        lateinit var myResources : Resources

        var mEnableDataClone = true

        var bmImage : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        lateinit var colorClass : ColorClass

        var mScaleFactor = 1.0f

        var offset: PointF = PointF(0.0f, 0.0f)

        var mCurrentPageID = 0

        var clickPos = PointF(0.0f, 0.0f)

        var mViewSize = Point(0, 0)

        var scopeIO = CoroutineScope(Dispatchers.IO)
    }

    // endregion

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= 30) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }*/

        myResources = resources

        colorClass = ColorClass()

        hideSystemUI()

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0){
                Handler(Looper.getMainLooper()).postDelayed({hideSystemUI()}, 3000)
            }
        }

        rand = Random(LocalDateTime.now().second + LocalDateTime.now().hour)

        val outPoint = Point()
        if (Build.VERSION.SDK_INT >= 19) {
            display!!.getRealSize(outPoint)
        } else {
            val metrics = windowManager.currentWindowMetrics.bounds
            outPoint.x = metrics.width()
            outPoint.y = metrics.height()
        }

        if (outPoint.y > outPoint.x) {
            mViewSize.y = outPoint.y
            mViewSize.x = outPoint.x
        } else {
            mViewSize.y = outPoint.x
            mViewSize.x = outPoint.y
        }

        mScaleFactor = (mViewSize.x - 8).toFloat() / width.toFloat()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        hideSystemUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.iconocat -> {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(iconocatURL)))

                return true
            }
            R.id.action_privacy -> {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(iconocatURL + privacyURL)))

                return true
            }
            R.id.action_advertdata -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun hideSystemUI() {
       // window.setDecorFitsSystemWindows(false)
/*
        window.setFlags(2048 or 1024 or 512 or 256 or 4 or 2,
            2048 or 1024 or 512 or 256 or 4 or 2)

        if (Build.VERSION.SDK_INT >= 19) {
            window.setFlags(2048 or 1024 or 512 or 256 or 4 or 2,
                2048 or 1024 or 512 or 256 or 4 or 2)
        } else {*/

        //supportActionBar?.hide()

            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
       // }

    }

    private fun setFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}
