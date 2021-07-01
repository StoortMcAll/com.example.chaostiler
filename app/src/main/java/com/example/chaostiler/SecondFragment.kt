package com.example.chaostiler

// region Variable Declaration

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.MainActivity.Companion.colorClass

// endregion


class SecondFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        super.onViewCreated(view, savedInstanceState)

        clonePixelData()

        tileImageView = view.findViewById<MyImageView>(R.id.tile_image_view)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        applyPaletteChangeToBitmap(pixelDataClone)

        view.findViewById<Button>(R.id.blur_left).setOnClickListener(){
            blurLeft()
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener() {
            colorClass.Decrease_SpreadID()

            var sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            Bitmap_ColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()

        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener() {
            colorClass.Increase_SpreadID()

            var sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            Bitmap_ColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()

        }

        view.findViewById<Button>(R.id.set_to_zero).setOnClickListener() {
            setToZero()
        }


        view.findViewById<Button>(R.id.addNewPalette).setOnClickListener() {
            colorClass.addNew_RandomPrimarysRange()

            var sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            Bitmap_ColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()
        }


        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


        view.findViewById<Button>(R.id.to_savewallpaper).setOnClickListener() {

            (this.activity as AppCompatActivity).supportActionBar?.hide()

            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }
    }
}