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
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone
import kotlinx.coroutines.*

// endregion


class FirstFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEnableDataClone = true

        var job : Job? = null

        tileImageView = view.findViewById(R.id.tile_image_view)
        mMaxHitsText = view.findViewById(R.id.first_maxhits)

        applyPaletteChangeToBitmap(pixelData)

        view.findViewById<Button>(R.id.run_square).setOnClickListener {
            makeInvisible(view)

            if (job == null || job?.isActive == false) {
                MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
                job = MainActivity.scopeIO.launch {
                    startNewRunFormula(true)
                }
            } else {
                job?.cancel(null)
            }
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener {
            colorClass.decreaseSpreadID()

            BitmapColorSpread.mNewColors = true

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener {
            colorClass.increaseSpreadID()

            BitmapColorSpread.mNewColors = true

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        view.findViewById<Button>(R.id.resume).setOnClickListener {
            if (doingCalc) {
                false.also { doingCalc = it }
                job?.cancel(null)

                makeVisible(view)
            }
            else{
                makeInvisible(view)
                MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
                job = MainActivity.scopeIO.launch {
                    startNewRunFormula(false)
                }

            }
        }

        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        makeVisible(view)
    }

    private fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
        MainActivity.scopeIO.launch {
            setTileViewBitmap(pixeldatacopy)
        }
    }

    private fun setTileViewBitmap(pixeldatacopy: PixelData) {
        aColors = buildPixelArrayFromColorsIncremental(pixeldatacopy)

        bmTexture.setPixels(aColors, 0,
            MainActivity.width, 0, 0,
            MainActivity.width,
            MainActivity.height)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
    }

    private fun makeVisible(view: View) {
        view.findViewById<ConstraintLayout>(R.id.constraintGenerators).isVisible = true
        val resumgen = view.findViewById<ConstraintLayout>(R.id.resume_generate)
        val resumbut = view.findViewById<Button>(R.id.resume)
        val chspal = view.findViewById<ConstraintLayout>(R.id.include_choose_palette)
        view.findViewById<Button>(R.id.add_new_palette).isVisible = false
        view.findViewById<Button>(R.id.add_new_palette2).isVisible = false
        val navi = view.findViewById<ConstraintLayout>(R.id.naviConstraint)

        if (pixelData.mMaxHits > 0) {
            navi.isVisible = true
            chspal.isVisible = true
            resumgen.isVisible = true
            mMaxHitsText.isVisible = true

            val value = pixelData.mMaxHits.toString()
            val iters = pixelData.mHitsCount.toString()
            var text = "Hits : Max - $value   Total - $iters"
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

    private fun makeInvisible(view: View) {
        view.findViewById<ConstraintLayout>(R.id.constraintGenerators).isVisible = false
        view.findViewById<ConstraintLayout>(R.id.resume_generate).isVisible = true
        view.findViewById<ConstraintLayout>(R.id.include_choose_palette).isVisible = true
        view.findViewById<ConstraintLayout>(R.id.naviConstraint).isVisible = false
        val resumbut = view.findViewById<Button>(R.id.resume)

        mMaxHitsText.isVisible = true

        val value = pixelData.mMaxHits.toString()
        val iters = pixelData.mHitsCount.toString()
        var text = "Hits : Max - $value   Total - $iters"
        mMaxHitsText.text = text.subSequence(0, text.length)

        text = "Pause"
        resumbut.text = text.subSequence(0, text.length)
    }

}