package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone

// endregion

class ThirdFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEnableDataClone = false

        tileImageView = view.findViewById(R.id.fullscreenImageView)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        view.findViewById<Button>(R.id.save_wallpaper).setOnClickListener {

            tileImageView.paintWallpaper(true)
        }

        view.findViewById<Button>(R.id.save_image).setOnClickListener {

            tileImageView.paintWallpaper(false)
        }

        view.findViewById<Button>(R.id.backto_secondFragment).setOnClickListener {

            findNavController().navigate(R.id.action_ThirdFragment_to_SecondFragment)
        }
    }
}