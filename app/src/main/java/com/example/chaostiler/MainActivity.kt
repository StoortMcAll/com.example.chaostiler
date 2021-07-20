package com.example.chaostiler

// region Variable Declaration

import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import kotlin.random.Random

// endregion


class MainActivity : AppCompatActivity() {

    companion object{
        enum class QuiltType {Square, Scratch, Hexagonal}

        enum class DataProcess {LINEAR, STATISTICAL}

        var quiltType = QuiltType.Square

        var rand  = Random(0)

        const val width = 512; const val height = 512

        const val mSeekbarMax = 256

        var mEnableDataClone = true

        var bmImage : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val bitmapColorSpread = BitmapColorSpread()

        var mScaleFactor = 1.0f

        var offset: PointF = PointF(0.0f, 0.0f)

        var mCurrentPageID = 0

        var clickPos = PointF(0.0f, 0.0f)

        var mViewSize = Point(0, 0)

        var scopeIO = CoroutineScope(Dispatchers.IO)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        hideSystemUI()

        rand = Random(LocalDateTime.now().second + LocalDateTime.now().hour)

        val outPoint = Point()
        if (Build.VERSION.SDK_INT >= 19) {
            // include navigation bar
            display!!.getRealSize(outPoint)
        } else {
            display!!.getSize(outPoint)
        }
        if (outPoint.y > outPoint.x) {
            mViewSize.y = outPoint.y
            mViewSize.x = outPoint.x
        } else {
            mViewSize.y = outPoint.x
            mViewSize.x = outPoint.y
        }

      /*  val ds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            baseContext.display
        } else {
            baseContext.display
        }*/

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
        return when (item.itemId) {

            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideSystemUI() {
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
    }


/*
    private fun setFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }*/
}
