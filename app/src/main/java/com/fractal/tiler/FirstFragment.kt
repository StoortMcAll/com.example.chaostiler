package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.mEnableDataClone
import com.fractal.tiler.MainActivity.Companion.QuiltType
import com.fractal.tiler.MainActivity.Companion.quiltType
import com.fractal.tiler.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

// endregion


class FirstFragment : Fragment() {

// region Variable Declaration

    val mThisPageID = 0

    private var _fragmentFirstBinding : FragmentFirstBinding? = null
    private val binding get() = _fragmentFirstBinding!!

    lateinit var viewThis : View

    lateinit var mPause: String
    lateinit var mResum: String

    lateinit var tileImageView : MyImageView
    lateinit var hitsInfoTextView : TextView

    var colorRangeRight : ImageButton? = null
    lateinit var colorRLayerPrev : LayerDrawable

    var colorRangeLeft : ImageButton? = null
    lateinit var colorRLayerNext : LayerDrawable

    var colorRangeMid : ImageButton? = null
    lateinit var colorRLayerMid : LayerDrawable
    lateinit var colorRDrawableMid : Drawable

    var runSquare : Button? = null
    var runScratch : Button? = null
    var runHex : Button? = null

    lateinit var roundedBitmapDrawable: RoundedBitmapDrawable

    val dpToPx = MainActivity.dpToPx

    companion object{
        var mHitsMinString : String = ""
        var mHitsMaxString : String = ""
    }

// endregion


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mpause = getString(R.string.stop_calc)
        mPause = mpause.substring(0, mpause.length)
        val mresum = getString(R.string.resume_calc)
        mResum = mresum.substring(0, mresum.length)

        mHitsMinString = getString(R.string.hitsmin_string)
        mHitsMaxString = getString(R.string.hitsmax_string)

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
        savedInstanceState: Bundle?): View {

        _fragmentFirstBinding = FragmentFirstBinding.inflate(inflater, container, false)

        MainActivity.mCurrentPageID = mThisPageID
        tileImageView = binding.tileImageGenerate.tileImageView
        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        colorRLayerPrev = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable
       // colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeDrawable

        colorRLayerMid = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_trans_stroke, null) as LayerDrawable
       // colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeDrawable

        colorRLayerNext = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable
        //colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeDrawable

        colorRangeRight = binding.colorRangeRight
        colorRangeMid = binding.colorRangeMid
        colorRangeLeft = binding.colorRangeLeft
        setAllColorRangeBackgrounds(false)

        colorRangeRight?.setBackground(colorRLayerPrev)
        colorRangeMid?.setForeground(colorRLayerMid)
        colorRangeLeft?.setBackground(colorRLayerNext)

        runSquare = binding.runSquare
        setRunStateBitmaps(runSquare, R.drawable.square_up, R.drawable.square_down)

        runScratch = binding.runScratch
        setRunStateBitmaps(runScratch, R.drawable.icon_up, R.drawable.icon_down)

        runHex = binding.runHex
        setRunStateBitmaps(runHex, R.drawable.hexagon_up, R.drawable.hexagon_down)

        hitsInfoTextView = binding.firstMaxhits

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewThis = view

        mEnableDataClone = true

        var job : Job? = null

        setTileImageView(tileImageView)// Set reference to MyImageView in RunGenTasks

        setHitsInfoTextView(hitsInfoTextView)// Set reference to TextView in RunGenTasks

        makeVisible(view)

        applyPaletteChangeToBitmap(pixelData)

        runSquare?.setOnClickListener {
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

        runScratch?.setOnClickListener {
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

        runHex?.setOnClickListener {
            makeInvisible(view)

            if (job == null || job?.isActive == false) {
                job = MainActivity.scopeIO.launch {
                    quiltType = QuiltType.HEXAGONAL
                    startNewRunFormula(true)
                }
            } else {
                job?.cancel(null)
            }
        }

        colorRangeRight?.setOnClickListener {
            MainActivity.colorClass.selectPrevColorRange()

            setAllColorRangeBackgrounds()

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        colorRangeLeft?.setOnClickListener {
            MainActivity.colorClass.selectNextColorRange()

            setAllColorRangeBackgrounds()

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        binding.resume.setOnClickListener {
            if (doingCalc) {
                false.also { doingCalc = it }
                job?.cancel(null)

                makeVisible(view)
            }
            else{
                makeInvisible(view)

                job = MainActivity.scopeIO.launch {
                    startNewRunFormula(false)
                }

            }
        }

        binding.switchToEditor.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_TabbedFragment)
        }

    }

    private fun setRunStateBitmaps(button : Button?, bitmapUpId: Int, bitmapDownId: Int){

        val bitmapDrawableDown = ResourcesCompat.getDrawable(resources, bitmapDownId, null) as BitmapDrawable
        val layerDown = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable
        layerDown.setDrawableByLayerId(R.id.layer_bitmap, getRoundedBitmap(bitmapDrawableDown.bitmap))

        val bitmapDrawableUp = ResourcesCompat.getDrawable(resources, bitmapUpId, null) as BitmapDrawable
        val layerUp = ResourcesCompat.getDrawable(resources, R.drawable.layer_bitmap_stroke, null) as LayerDrawable
        layerUp.setDrawableByLayerId(R.id.layer_bitmap, getRoundedBitmap(bitmapDrawableUp.bitmap))

        val newStates = StateListDrawable()

        newStates.addState(intArrayOf(-android.R.attr.state_pressed, android.R.attr.state_enabled), layerUp)
        newStates.addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), layerDown)
        newStates.addState(intArrayOf(-android.R.attr.state_enabled), ColorDrawable(Color.WHITE))

        button?.setForeground(newStates)
    }

    private fun setPrevColorRangeBackground(invalidate : Boolean = true) {
        //colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeDrawable
        colorRLayerPrev.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(MainActivity.colorClass.aPrevRange.colorRangeBitmap)
        )

        if (invalidate)
            colorRangeRight?.invalidate()
    }
    private fun setColorRangeMidBackground(invalidate : Boolean = true) {
        //colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(resources)
        colorRLayerMid.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(MainActivity.colorClass.aCurrentRange.colorRangeBitmap)
        )

        if (invalidate)
            colorRangeMid?.invalidate()
    }
    private fun setNextColorRangeBackground(invalidate : Boolean = true) {
        //colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeDrawable
        colorRLayerNext.setDrawableByLayerId(
            R.id.layer_bitmap,
            getRoundedBitmap(MainActivity.colorClass.aNextRange.colorRangeBitmap)
        )

        if (invalidate)
            colorRangeLeft?.invalidate()
    }

    private fun getRoundedBitmap(bitmap : Bitmap) : RoundedBitmapDrawable {
        roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedBitmapDrawable.cornerRadius = dpToPx

        return roundedBitmapDrawable
    }
    private fun setAllColorRangeBackgrounds(invalidate : Boolean = true){
        setPrevColorRangeBackground(invalidate)
        setColorRangeMidBackground(invalidate)
        setNextColorRangeBackground(invalidate)
    }

    private fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
        CoroutineScope(Dispatchers.IO).launch {
            setTileViewBitmap(pixeldatacopy)
        }
    }

    private fun setTileViewBitmap(pixeldatacopy: PixelData) {
        aColors = buildPixelArrayFromIncrementalColors(pixeldatacopy)

        bmTexture.setPixels(aColors, 0,
            MainActivity.width, 0, 0,
            MainActivity.width,
            MainActivity.height)

        CoroutineScope(Dispatchers.Main).launch {
            tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
        }
    }

    private fun makeVisible(view: View) {
        val resumbut = view.findViewById<Button>(R.id.resume)
        val chspal = view.findViewById<ConstraintLayout>(R.id.pal_linearlayout)
        val navi = view.findViewById<Button>(R.id.switch_to_editor)

        if (pixelData.mMaxHits > 0) {
            if (!doingCalc) {
                view.findViewById<ConstraintLayout>(R.id.generate_linearlayout).isVisible = true
                navi.isVisible = true
                chspal.isVisible = true
                resumbut.isVisible = true
                hitsInfoTextView.isVisible = true

                val mmin = pixelData.mMinHits.toString()
                val mmax = pixelData.mMaxHits.toString()

                val text = mHitsMinString+ " "  + mmin.padStart(4)+ " "  + mHitsMaxString + " " + mmax.padStart(4)
                hitsInfoTextView.text = text.subSequence(0, text.length)

                resumbut.foreground = ResourcesCompat.getDrawable(MainActivity.myResources, R.drawable.resume_states, null)
            }
            else{
                view.findViewById<ConstraintLayout>(R.id.generate_linearlayout).setVisibility(View.INVISIBLE)
                navi.setVisibility(View.INVISIBLE)
               // chspal.isVisible = true
                resumbut.isVisible = true
                hitsInfoTextView.isVisible = true

                resumbut.foreground = ResourcesCompat.getDrawable(MainActivity.myResources, R.drawable.pause_states, null)
            }
        }
        else{
            view.findViewById<ConstraintLayout>(R.id.generate_linearlayout).isVisible = true
            navi.setVisibility(View.INVISIBLE)
            chspal.setVisibility(View.INVISIBLE)
          //  resumbut.setVisibility(View.INVISIBLE)
            hitsInfoTextView.setVisibility(View.INVISIBLE)
        }
    }

    private fun makeInvisible(view: View) {
        view.findViewById<ConstraintLayout>(R.id.generate_linearlayout).setVisibility(View.INVISIBLE)
        view.findViewById<ConstraintLayout>(R.id.pal_linearlayout).isVisible = true
        view.findViewById<Button>(R.id.switch_to_editor).setVisibility(View.INVISIBLE)
        val resumbut = view.findViewById<Button>(R.id.resume)
        resumbut.isVisible = true

        hitsInfoTextView.isVisible = true

        val mmin = pixelData.mMinHits.toString()
        val mmax = pixelData.mMaxHits.toString()

        val text = mHitsMinString+ " "  + mmin.padStart(4)+ " "  + mHitsMaxString + " " + mmax.padStart(4)
        hitsInfoTextView.text = text.subSequence(0, text.length)

        resumbut.foreground = ResourcesCompat.getDrawable(resources, R.drawable.pause_states, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentFirstBinding = null
    }

}