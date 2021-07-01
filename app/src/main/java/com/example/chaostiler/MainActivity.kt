package com.example.chaostiler

// region Variable Declaration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import kotlin.random.Random

// endregion


class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var mainContext : Context

        lateinit var wm : WindowMetrics

        lateinit var tv_dynamic : TextView

        var rand  = Random(0)

        var mainCounter = 0

        val width = 512; val height = 512

        var mSeekbarMax = 256

        var bmImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        var mScaleFactor = 1.0f
        var offset: PointF = PointF(0.0f, 0.0f)

        var colorClass = ColorClass()

        var clickPos = PointF(0.0f, 0.0f)

        var mViewSize = Point(0, 0)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        hideSystemUI()

        tv_dynamic = TextView(this)
        tv_dynamic.textSize = 20f
        tv_dynamic.width = 0
        tv_dynamic.text="@string/max_hits"
        tv_dynamic.isAllCaps = false
        tv_dynamic.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        rand = Random(LocalDateTime.now().second + LocalDateTime.now().hour)

        var vl = IntArray(10)
        for (i in 0 until 10){
            vl[i] = rand.nextInt()
        }
        mainContext = this.baseContext

        var disp = mainContext.display

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

        val ds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            baseContext.display
        } else {
            baseContext.display
        }

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

    private fun setFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}