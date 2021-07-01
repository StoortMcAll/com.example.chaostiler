package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.MainActivity.Companion.colorClass

// endregion


class FirstFragment : Fragment() {

    lateinit var mStopCalc : Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeVisible(view)

        tileImageView = view.findViewById(R.id.tile_image_view)

        mStopCalc = view.findViewById(R.id.resume)

        applyPaletteChangeToBitmap(pixelData)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        view.findViewById<Button>(R.id.run_square).setOnClickListener {
            GenerateFragment.startNew = true

            findNavController().navigate(R.id.action_FirstFragment_to_GenerateFragment)
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener() {
            colorClass.Decrease_SpreadID()

            Bitmap_ColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelData)
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener() {
            colorClass.Increase_SpreadID()

            Bitmap_ColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelData)
        }


        view.findViewById<Button>(R.id.resume).setOnClickListener {
            GenerateFragment.startNew = false

            findNavController().navigate(R.id.action_FirstFragment_to_GenerateFragment)
        }

        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {

            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

    }

    fun makeVisible(view: View) {
        if (pixelData.mMaxHits > 0) {
            var resum = view.findViewById<ConstraintLayout>(R.id.resume_generate)
            var navi = view.findViewById<ConstraintLayout>(R.id.include_choose_palette)
            var chspal = view.findViewById<ConstraintLayout>(R.id.naviConstraint)
            if (!navi.isVisible) navi.isVisible = true
            if (!chspal.isVisible) chspal.isVisible = true
            if (!resum.isVisible) resum.isVisible = true
        }
    }

}