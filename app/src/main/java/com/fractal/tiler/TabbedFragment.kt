package com.fractal.tiler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class TabbedFragment : Fragment() {
    private lateinit var pTabs: TabLayout
    private lateinit var pViewPager: ViewPager
    private lateinit var pagerAdapters: PagerAdapters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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

        view.findViewById<Button>(R.id.backto_firstfragment).setOnClickListener {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        view.findViewById<Button>(R.id.switch_to_editor).setOnClickListener {
            findNavController().navigate(R.id.action_TabbedFragment_to_SecondFragment)
        }
    }
}