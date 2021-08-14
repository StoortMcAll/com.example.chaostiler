package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.FirstFragment.Companion.tileImageView
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// endregion

class ThirdFragment : Fragment() {

    val mThisPageID = 2

    var isBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!isBusy) {
                findNavController().navigate(R.id.action_ThirdFragment_to_SecondFragment)
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

        tileImageView = view.findViewById(R.id.fullscreenImageView)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        view.findViewById<Button>(R.id.save_wallpaper).setOnClickListener {
            val shaderView = view.findViewById<TextView>(R.id.shader)

            if (!isBusy) {
                isBusy = true

                shaderView.visibility = View.VISIBLE
                shaderView.invalidate()

                CoroutineScope(Dispatchers.IO).launch {
                    tileImageView.paintWallpaper(true)

                    CoroutineScope(Dispatchers.Main).launch {
                        shaderView.visibility = View.INVISIBLE
                        shaderView.invalidate()
                    }

                    isBusy = false
                }
            }
        }

        view.findViewById<Button>(R.id.save_image).setOnClickListener {
            val shaderView = view.findViewById<TextView>(R.id.shader)

            if (!isBusy) {
                isBusy = true

                shaderView.visibility = View.VISIBLE
                shaderView.invalidate()

                CoroutineScope(Dispatchers.IO).launch {
                    tileImageView.paintWallpaper(false)

                    CoroutineScope(Dispatchers.Main).launch {
                        shaderView.visibility = View.INVISIBLE
                        shaderView.invalidate()
                    }

                    isBusy = false
                }
            }
        }

        view.findViewById<Button>(R.id.back_to_secondFragment).setOnClickListener {
            if (!isBusy) {
                findNavController().navigate(R.id.action_ThirdFragment_to_SecondFragment)
            }
        }
    }
}