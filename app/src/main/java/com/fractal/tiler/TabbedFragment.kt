package com.fractal.tiler

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

private lateinit var tileImageView : MyImageView

class TabbedFragment : Fragment() {

    private lateinit var pTabs: TabLayout
    private lateinit var pViewPager: ViewPager
    private lateinit var pagerAdapters: PagerAdapters


    val mThisPageID = 1

    var isBusy = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivity.mCurrentPageID = mThisPageID

        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        callback.isEnabled
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        if (MainActivity.mEnableDataClone) {
            pixelDataClone = pixelData.clone()

            //pixelDataClone.recalcScaledHitStats()
            pixelDataClone.calcTangentScale()

            MainActivity.mEnableDataClone = false
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tabbed, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pTabs = view.findViewById(R.id.mytabs)
        pViewPager = view.findViewById(R.id.myPagerView)
        pagerAdapters = PagerAdapters(childFragmentManager)
        /**set Fragment List*/
        pagerAdapters.addFragment(DataFragment(), "Data")
        pagerAdapters.addFragment(ColorFragment(), "Palette")

        /** set view page adapter*/
        pViewPager.adapter = pagerAdapters
        /** set tabs*/
        pTabs.setupWithViewPager(pViewPager)
        /**ok now add icon*/
      /*  pTabs.getTabAt(0)!!.setIcon(R.drawable.ic__home)
        pTabs.getTabAt(1)!!.setText("Tester")//Icon(R.drawable.ic_search)
        pTabs.getTabAt(2)!!.setIcon(R.drawable.ic_notifications)
    */

        tileImageView = view.findViewById(R.id.tile_image_view)

        tileImageView.setBitmap(bmTexture.copy(Bitmap.Config.ARGB_8888, false))

        setTileImageView(tileImageView)// Set reference to MyImageView in RunGenTasks

        pixelDataClone.calcTangentScale()

        setTileViewBitmap(pixelDataClone, true)

        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {
            findNavController().navigate(R.id.action_TabbedFragment_to_ThirdFragment)
        }
    }

}
