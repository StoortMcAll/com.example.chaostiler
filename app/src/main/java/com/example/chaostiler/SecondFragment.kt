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
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone

// endregion


class SecondFragment : Fragment() {

    private lateinit var seekbar: MySeekbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        super.onViewCreated(view, savedInstanceState)

        if (mEnableDataClone) {
            pixelDataClone = pixelData.Clone()
        }

        seekbar = view.findViewById(R.id.seekBar)
        seekbar.progress = colorClass.getCurrentRange().prog

        tileImageView = view.findViewById(R.id.tile_image_view)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        applyPaletteChangeToBitmap(pixelDataClone)

        view.findViewById<Button>(R.id.undo_all_changes).setOnClickListener {
            undoAllChanges()
        }


        view.findViewById<Button>(R.id.blur_left).setOnClickListener {
            blurLeft()
        }

        view.findViewById<Button>(R.id.blur_right).setOnClickListener {
            blurRight()
        }


        view.findViewById<Button>(R.id.palette_left).setOnClickListener {
            colorClass.decreaseSpreadID()

            val sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            sk?.progress = colorClass.getCurrentRange().prog

            BitmapColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener {
            colorClass.increaseSpreadID()

            val sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            sk?.progress = colorClass.getCurrentRange().prog

            BitmapColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()
        }


        view.findViewById<Button>(R.id.set_to_zero).setOnClickListener {
            setToZero()
        }


        view.findViewById<Button>(R.id.add_new_palette).setOnClickListener {
            colorClass.addNewRandomPrimariesRange()

            val sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            sk?.progress = colorClass.getCurrentRange().prog

            BitmapColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()
        }

        view.findViewById<Button>(R.id.add_new_palette2).setOnClickListener {
            colorClass.addNewRandomColorsRange()

            val sk = activity?.findViewById<MySeekbar>(R.id.seekBar)

            sk?.progress = colorClass.getCurrentRange().prog

            BitmapColorSpread.mNewColors = true

            applyPaletteChangeToBitmap(pixelDataClone)

            sk?.invalidate()
        }


        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


        view.findViewById<Button>(R.id.to_savewallpaper).setOnClickListener {

            (this.activity as AppCompatActivity).supportActionBar?.hide()

            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }
    }
}