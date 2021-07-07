package com.example.chaostiler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.chaostiler.FirstFragment.Companion.mMaxHitsText
import com.example.chaostiler.MainActivity.Companion.scopeIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GenerateFragment : Fragment() {
    companion object{
        var startNew = true

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_generate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      /*  if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()*/

        tileImageView = view.findViewById(R.id.tile_image_view)

        mMaxHitsText = view.findViewById(R.id.max_hits)

        jobRunning = true
        doingCalc = false
        //maxCount = 1

        view.findViewById<Button>(R.id.resume).setOnClickListener {
            doingCalc = false

           // while(jobRunning){}

            //findNavController().navigate(R.id.action_GenerateFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener() {
            MainActivity.colorClass.Decrease_SpreadID()

            Bitmap_ColorSpread.mNewColors = true
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener() {
            MainActivity.colorClass.Increase_SpreadID()

            Bitmap_ColorSpread.mNewColors = true
        }
/*
        scopeIO.launch {
            startNew_RunFormula(startNew)
            jobRunning = false
            //findNavController().navigate(R.id.action_GenerateFragment_to_FirstFragment)
        }*/

    }

}