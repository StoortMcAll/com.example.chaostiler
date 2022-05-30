package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.colorClass
import com.fractal.tiler.MainActivity.Companion.mEnableDataClone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// endregion

class ThirdFragment : Fragment() {

    val mThisPageID = 2

    lateinit var imageView : MyImageView
    lateinit var shaderView : View

    var isBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivity.mCurrentPageID = mThisPageID

        (this.activity as AppCompatActivity).supportActionBar?.hide()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!isBusy) {
                findNavController().navigate(R.id.action_ThirdFragment_to_TabbedFragment)
            }
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MainActivity.mCurrentPageID = mThisPageID

        mEnableDataClone = false

        shaderView = view.findViewById(R.id.shader)

        imageView = view.findViewById(R.id.fullscreenImageView)

        imageView.offsetForFullscreen(true)

        setTileImageView(imageView)

        imageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        val mainColor = colorClass.aCurrentRange.aRgbColorsList[0]
        val hsvValues = FloatArray(3)
        Color.colorToHSV(mainColor,hsvValues)

        var useLightText = if(hsvValues[2] < 0.5f) false else true

        val applySmooth : Button = view.findViewById(R.id.apply_smooth)
        val saveWallpaper : Button = view.findViewById(R.id.save_wallpaper)
        val saveImage : Button = view.findViewById(R.id.save_image)
        val backtoSecond : Button = view.findViewById(R.id.back_to_secondFragment)

        setButtonColors(applySmooth, useLightText)
        setButtonColors(saveWallpaper, useLightText)
        setButtonColors(saveImage, useLightText)
        setButtonColors(backtoSecond, useLightText)

        applySmooth.setOnClickListener {
            if (!isBusy) {
                isBusy = true

                shaderView.visibility = View.VISIBLE
                shaderView.invalidate()

                CoroutineScope(Dispatchers.Main).launch {

                    when (MainActivity.filter) {
                        MainActivity.Companion.ImageFilter.Blur -> {
                            aColors = Blur.doImageFilter()
                        }
                        MainActivity.Companion.ImageFilter.Gaussian -> {
                            aColors = Gaussian.doImageFilter()
                        }
                        MainActivity.Companion.ImageFilter.Motion -> {
                            aColors = Motion.doImageFilter()
                        }
                        MainActivity.Companion.ImageFilter.BoxBlur -> {
                            aColors = Sharpen.doImageFilter()
                        }
                        MainActivity.Companion.ImageFilter.Median -> {
                            aColors = Smooth.doImageFilter()
                        }
                    }

                    bmTexture.setPixels(
                        aColors, 0,
                        MainActivity.width, 0, 0,
                        MainActivity.width,
                        MainActivity.height
                    )

                    imageView.setBitmap(bmTexture)

                    shaderView.visibility = View.INVISIBLE
                    shaderView.invalidate()

                    isBusy = false
                }

            }
        }

        saveWallpaper.setOnClickListener {
            if (!isBusy) {
                isBusy = true

                shaderView.visibility = View.VISIBLE
                shaderView.invalidate()

                CoroutineScope(Dispatchers.Main).launch {
                    val job: Job = CoroutineScope(Dispatchers.IO).launch {
                        //for (i in 0..4) {
                            imageView.paintWallpaper(true)
                       // }

                        isBusy = false
                    }

                    job.join()

                    shaderView.visibility = View.INVISIBLE
                    shaderView.invalidate()
                }
            }
        }

        saveImage.setOnClickListener {
            if (!isBusy) {
                isBusy = true

                shaderView.visibility = View.VISIBLE
                shaderView.invalidate()

                CoroutineScope(Dispatchers.IO).launch {
                    imageView.paintWallpaper(false)

                    CoroutineScope(Dispatchers.Main).launch {
                        shaderView.visibility = View.INVISIBLE
                        shaderView.invalidate()
                    }

                    isBusy = false
                }
            }
        }

        backtoSecond.setOnClickListener {
            if (!isBusy) {
                imageView.offsetForFullscreen(false)

                findNavController().navigate(R.id.action_ThirdFragment_to_TabbedFragment)
            }
        }
    }

    private fun setButtonColors(button : Button, useLightText : Boolean){
        val transCol : Int
        val textCol : Int

        if(useLightText){
            transCol = Color.argb(64, 0,0,0)
            textCol = Color.argb(255, 252,239,232)
        } else{
            transCol = Color.argb(64, 255,255,255)
            textCol = Color.argb(255, 63,60,58)
        }

        button.setBackgroundColor(transCol)
        button.setTextColor(textCol)

        button.invalidate()
    }
}