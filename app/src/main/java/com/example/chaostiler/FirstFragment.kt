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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.MainActivity.Companion.bitmapColorSpread
import com.example.chaostiler.MainActivity.Companion.mEnableDataClone
import com.example.chaostiler.MainActivity.Companion.QuiltType
import com.example.chaostiler.MainActivity.Companion.quiltType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

// endregion


class FirstFragment : Fragment() {

    val mThisPageID = 0

    lateinit var viewThis : View

    lateinit var mPause: String
    lateinit var mResum: String


    companion object{
        lateinit var tileImageView : MyImageView
        lateinit var mMaxHitsText : TextView

        lateinit var mHits : String
        lateinit var mTotal : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mpause = getString(R.string.stop_calc)
        mPause = mpause.substring(0, mpause.length)
        val mresum = getString(R.string.resume_calc)
        mResum = mresum.substring(0, mresum.length)

        mHits = getString(R.string.hits_string)
        mTotal = getString(R.string.total_string)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (doingCalc) {
                false.also { doingCalc = it }
                job?.cancel(null)

                makeVisible(viewThis)
            }
            else {
                exitProcess(0)
            }
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewThis = view

        MainActivity.mCurrentPageID = mThisPageID

        mEnableDataClone = true

        var job : Job? = null

        tileImageView = view.findViewById(R.id.tile_image_view)
        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        mMaxHitsText = view.findViewById(R.id.first_maxhits)

        makeVisible(view)

        applyPaletteChangeToBitmap(pixelData)

        view.findViewById<Button>(R.id.run_square).setOnClickListener {
            makeInvisible(view)

            if (job == null || job?.isActive == false) {
                job = MainActivity.scopeIO.launch {
                    quiltType = QuiltType.SQUARE
                    startNewRunFormula(true)
                }
            } else {
                job?.cancel(null)
            }
        }

        view.findViewById<Button>(R.id.run_scratch).setOnClickListener {
            makeInvisible(view)

            if (job == null || job?.isActive == false) {
                job = MainActivity.scopeIO.launch {
                    quiltType = QuiltType.SCRATCH
                    startNewRunFormula(true)
                }
            } else {
                job?.cancel(null)
            }
        }

        view.findViewById<Button>(R.id.run_hex).setOnClickListener {
            makeInvisible(view)

            if (job == null || job?.isActive == false) {
                //MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
                job = MainActivity.scopeIO.launch {
                    quiltType = QuiltType.HEXAGONAL
                    startNewRunFormula(true)
                }
            } else {
                job?.cancel(null)
            }
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener {
            bitmapColorSpread.prevPalette()

            bitmapColorSpread.updateColorSpreadBitmap(pixelData)

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener {
            bitmapColorSpread.nextPalette()

            bitmapColorSpread.updateColorSpreadBitmap(pixelData)

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
                //MainActivity.scopeIO = CoroutineScope(Dispatchers.IO)
                job = MainActivity.scopeIO.launch {
                    startNewRunFormula(false)
                }

            }
        }

        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

    }

    private fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
        CoroutineScope(Dispatchers.IO).launch {
            setTileViewBitmap(pixeldatacopy)
        }
    }

    private fun setTileViewBitmap(pixeldatacopy: PixelData) {
        if (bitmapColorSpread.aCurrentRange.dataProcess == MainActivity.Companion.DataProcess.LINEAR){
            aColors = buildPixelArrayFromIncrementalColors(pixeldatacopy)
        }
        else{
            aColors = buildPixelArrayFromStatisticalColors(pixeldatacopy)
        }

        bmTexture.setPixels(aColors, 0,
            MainActivity.width, 0, 0,
            MainActivity.width,
            MainActivity.height)

        CoroutineScope(Dispatchers.Main).launch {
            tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
        }
    }

    private fun makeVisible(view: View) {
        val generate = view.findViewById<ConstraintLayout>(R.id.constraintGenerators)
        val resumgen = view.findViewById<ConstraintLayout>(R.id.resume_generate)
        val resumbut = view.findViewById<Button>(R.id.resume)
        view.findViewById<ConstraintLayout>(R.id.data_colour_constraint).isVisible = false
        val chspal = view.findViewById<ConstraintLayout>(R.id.include_choose_palette)
        view.findViewById<Button>(R.id.add_new_palette).isVisible = false
        view.findViewById<Button>(R.id.add_new_palette2).isVisible = false
        val navi = view.findViewById<ConstraintLayout>(R.id.naviConstraint)

        if (pixelData.mMaxHits > 0) {
            if (!doingCalc) {
                generate.isVisible = true
                navi.isVisible = true
                chspal.isVisible = true
                resumgen.isVisible = true
                mMaxHitsText.isVisible = true

                val value = pixelData.mMaxHits.toString()
                val iters = pixelData.mHitsCount.toString()

                val text = mHits + " " + value + " " + mTotal + " " + iters
                mMaxHitsText.text = text.subSequence(0, text.length)

                resumbut.text = mResum
            }
            else{
                generate.isVisible = false
                navi.isVisible = false
                chspal.isVisible = true
                resumgen.isVisible = true
                mMaxHitsText.isVisible = true

                resumbut.text = mPause
            }
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
        view.findViewById<ConstraintLayout>(R.id.data_colour_constraint).isVisible = false
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