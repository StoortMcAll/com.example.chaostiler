package com.fractal.tiler

// region Variable Declaration

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
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

    var colorRangeRight : Button? = null
    lateinit var colorRLayerRight : LayerDrawable
    lateinit var colorRDrawableRight : Drawable

    var colorRangeLeft : Button? = null
    lateinit var colorRLayerLeft : LayerDrawable
    lateinit var colorRDrawableLeft : Drawable

    var colorRangeMid : Button? = null
    lateinit var colorRLayerMid : LayerDrawable
    lateinit var colorRDrawableMid : Drawable

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
        savedInstanceState: Bundle?): View? {

        _fragmentFirstBinding = FragmentFirstBinding.inflate(inflater, container, false)

        MainActivity.mCurrentPageID = mThisPageID

        tileImageView = binding.tileImageGenerate.tileImageView
        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        colorRLayerRight =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_right, null) as LayerDrawable
        colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeDrawable

        colorRLayerMid =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_mid, null) as LayerDrawable
        colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeDrawable

        colorRLayerLeft =
            ResourcesCompat.getDrawable(resources, R.drawable.layer_colorrange_left, null) as LayerDrawable
        colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeDrawable

        colorRangeRight = binding.colorRangeRight
        colorRangeRight?.setForeground(colorRLayerRight)
        setColorRangeRightBackground()

        colorRangeMid = binding.colorRangeMid
        setColorRangeMidBackground()

        colorRangeLeft = binding.colorRangeLeft
        setColorRangeLeftBackground()

        hitsInfoTextView = binding.firstMaxhits

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewThis = view

        mEnableDataClone = true

        var job : Job? = null

/*
        tileImageView = view.findViewById(R.id.tile_image_view)
        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))
*/

        setTileImageView(tileImageView)// Set reference to MyImageView in RunGenTasks

        //hitsInfoTextView = view.findViewById(R.id.first_maxhits)

        setHitsInfoTextView(hitsInfoTextView)// Set reference to TextView in RunGenTasks

        makeVisible(view)

        applyPaletteChangeToBitmap(pixelData)

        binding.runSquare.setOnClickListener {
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

        binding.runScratch.setOnClickListener {
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

        binding.runHex.setOnClickListener {
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

        colorRangeRight?.setOnClickListener {
            MainActivity.colorClass.selectPrevColorRange()

            setAllColorRangeBackgrounds()

            //bitmapColorSpread.updateColorSpreadBitmap(pixelData)

            if (!doingCalc) {
                applyPaletteChangeToBitmap(pixelData)
            }
        }

        colorRangeLeft?.setOnClickListener {
            MainActivity.colorClass.selectNextColorRange()

            setAllColorRangeBackgrounds()

            //bitmapColorSpread.updateColorSpreadBitmap(pixelData)

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
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

    }


    private fun setColorRangeRightBackground(){
        colorRDrawableRight = MainActivity.colorClass.aPrevRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerRight.setDrawableByLayerId(R.id.layer_image_r, colorRDrawableRight)
        //colorRangeRight?.setForeground(colorRLayerRight)
        colorRangeRight?.invalidate()
    }
    private fun setColorRangeMidBackground(){
        colorRDrawableMid = MainActivity.colorClass.aCurrentRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerMid.setDrawableByLayerId(R.id.layer_image_m, colorRDrawableMid)
        colorRangeMid?.setForeground(colorRLayerMid)
        colorRangeMid?.invalidate()
    }
    private fun setColorRangeLeftBackground(){
        colorRDrawableLeft = MainActivity.colorClass.aNextRange.colorRangeBitmap.toDrawable(MainActivity.myResources)
        colorRLayerLeft.setDrawableByLayerId(R.id.layer_image_l, colorRDrawableLeft)
        colorRangeLeft?.setForeground(colorRLayerLeft)
        colorRangeLeft?.invalidate()
    }

    private fun setAllColorRangeBackgrounds(){
        setColorRangeRightBackground()
        setColorRangeMidBackground()
        setColorRangeLeftBackground()
    }

    private fun applyPaletteChangeToBitmap(pixeldatacopy : PixelData){
        CoroutineScope(Dispatchers.IO).launch {
            setTileViewBitmap(pixeldatacopy)
        }
    }

    private fun setTileViewBitmap(pixeldatacopy: PixelData) {
        if (MainActivity.colorClass.aCurrentRange.dataProcess == MainActivity.Companion.DataProcess.LINEAR){
            aColors = buildPixelArrayFromIncrementalColors(pixeldatacopy)
        }
        else{
            aColors = buildPixelArrayFromSinwave(pixeldatacopy)
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
        val resumbut = view.findViewById<Button>(R.id.resume)
        view.findViewById<FrameLayout>(R.id.colourstyle_framelayout).setVisibility(View.INVISIBLE)
        val chspal = view.findViewById<LinearLayout>(R.id.pal_linearlayout)
        //view.findViewById<Button>(R.id.add_new_palette).setVisibility(View.INVISIBLE)
        //view.findViewById<Button>(R.id.add_new_palette2).setVisibility(View.INVISIBLE)
        val navi = view.findViewById<Button>(R.id.switch_to_editor)

        if (pixelData.mMaxHits > 0) {
            if (!doingCalc) {
                view.findViewById<LinearLayout>(R.id.generate_linearlayout).isVisible = true
                navi.isVisible = true
                chspal.isVisible = true
                resumbut.isVisible = true
                hitsInfoTextView.isVisible = true

                val mmin = pixelData.mMinHits.toString()
                val mmax = pixelData.mMaxHits.toString()
                //val iters = pixelData.mHitsCount.toString()

                val text = mHitsMinString+ " "  + mmin.padStart(4)+ " "  + mHitsMaxString + " " + mmax.padStart(4)
                hitsInfoTextView.text = text.subSequence(0, text.length)

                resumbut.foreground = resources.getDrawable(R.drawable.resume_states)
            }
            else{
                view.findViewById<LinearLayout>(R.id.generate_linearlayout).setVisibility(View.INVISIBLE)
                navi.setVisibility(View.INVISIBLE)
                chspal.isVisible = true
                resumbut.isVisible = true
                hitsInfoTextView.isVisible = true

                resumbut.foreground = resources.getDrawable(R.drawable.pause_states)
            }
        }
        else{
            view.findViewById<LinearLayout>(R.id.generate_linearlayout).isVisible = true
            navi.setVisibility(View.INVISIBLE)
            chspal.setVisibility(View.INVISIBLE)
            resumbut.setVisibility(View.INVISIBLE)
            hitsInfoTextView.setVisibility(View.INVISIBLE)
        }
    }

    private fun makeInvisible(view: View) {
        view.findViewById<LinearLayout>(R.id.generate_linearlayout).setVisibility(View.INVISIBLE)
        view.findViewById<FrameLayout>(R.id.colourstyle_framelayout).setVisibility(View.INVISIBLE)
        view.findViewById<LinearLayout>(R.id.pal_linearlayout).isVisible = true
        view.findViewById<Button>(R.id.switch_to_editor).setVisibility(View.INVISIBLE)
        val resumbut = view.findViewById<Button>(R.id.resume)
        resumbut.isVisible = true

        hitsInfoTextView.isVisible = true

        val mmin = pixelData.mMinHits.toString()
        val mmax = pixelData.mMaxHits.toString()

        val text = mHitsMinString+ " "  + mmin.padStart(4)+ " "  + mHitsMaxString + " " + mmax.padStart(4)
        hitsInfoTextView.text = text.subSequence(0, text.length)

        resumbut.foreground = resources.getDrawable(R.drawable.pause_states)
    }

}