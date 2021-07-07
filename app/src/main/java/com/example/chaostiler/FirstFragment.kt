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
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.MainActivity.Companion.colorClass
import kotlinx.coroutines.*

// endregion


class FirstFragment : Fragment() {
    lateinit var mStartSquare : Button
    lateinit var mResumeButton : Button

    companion object {
        var newRunCount = 0

        lateinit var mMaxHitsText: TextView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var job : Job? = null

        tileImageView = view.findViewById(R.id.tile_image_view)
        mMaxHitsText = view.findViewById(R.id.first_maxhits)
        mStartSquare = view.findViewById(R.id.run_square)
        mResumeButton = view.findViewById(R.id.resume)

        applyPaletteChangeToBitmap(pixelData)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        mStartSquare.setOnClickListener {
            makeInvisible(view)

            if (job != null){
                if (job?.isActive == true) {
                    job?.cancel(null)
                }
            }
            MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
            job = MainActivity.scopeIO.launch {
                startNew_RunFormula(true, newRunCount)
                newRunCount++
            }
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

        mResumeButton.setOnClickListener {
            if (mResumeButton.text == "Pause") {
                doingCalc = false
                job?.cancel(null)
                //while (job?.isCompleted == false) { }
                makeVisible(view)
            }
            else{
                makeInvisible(view)
                MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
                job = MainActivity.scopeIO.launch {
                    startNew_RunFormula(false, newRunCount)
                }

            }
        }

        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        makeVisible(view)
    }

    fun makeVisible(view: View) {
        view.findViewById<ConstraintLayout>(R.id.constraintGenerators).isVisible = true

        var resumgen = view.findViewById<ConstraintLayout>(R.id.resume_generate)
        var resumbut = view.findViewById<Button>(R.id.resume)
        var chspal = view.findViewById<ConstraintLayout>(R.id.include_choose_palette)
        var navi = view.findViewById<ConstraintLayout>(R.id.naviConstraint)

        if (pixelData.mMaxHits > 0) {
            navi.isVisible = true
            chspal.isVisible = true
            resumgen.isVisible = true
            mMaxHitsText.isVisible = true

            val value = pixelData.mMaxHits.toString()
            var text = "Max Hits  $value"
            mMaxHitsText.text = text.subSequence(0, text.length)

            text = "Resume"
            resumbut.text = text.subSequence(0, text.length)
        }
        else{
            navi.isVisible = false
            chspal.isVisible = false
            resumgen.isVisible = false
            mMaxHitsText.isVisible = false
        }
    }

    fun makeInvisible(view: View) {
        var resumgen = view.findViewById<ConstraintLayout>(R.id.resume_generate)
        var resumbut = view.findViewById<Button>(R.id.resume)
        var chspal = view.findViewById<ConstraintLayout>(R.id.include_choose_palette)
        var navi = view.findViewById<ConstraintLayout>(R.id.naviConstraint)

        navi.isVisible = false
        chspal.isVisible = true
        resumgen.isVisible = true
        mMaxHitsText.isVisible = true

        val value = pixelData.mMaxHits.toString()
        var text = "Max Hits  $value"
        mMaxHitsText.text = text.subSequence(0, text.length)

        text = "Pause"
        resumbut.text = text.subSequence(0, text.length)
    }

}