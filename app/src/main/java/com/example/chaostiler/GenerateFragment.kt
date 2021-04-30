package com.example.chaostiler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GenerateFragment : Fragment() {
    private var doText: MutableLiveData<Boolean> = MutableLiveData(false)


    companion object{

        lateinit var genView : ConstraintLayout
        var startNew = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_generate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        tileImageView = view.findViewById(R.id.tile_image_view)

        maxHitsView = view.findViewById(R.id.max_hits)

        genView = view.findViewById<ConstraintLayout>(R.id.generateConstraint)

        doingCalc = false
        //maxCount = 1

        if (startNew) startNew_RunFormula()
        else resume_RunFormula()

        view.findViewById<Button>(R.id.resume).setOnClickListener {
            doingCalc = false
            //maxCount = 1

            findNavController().navigate(R.id.action_GenerateFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.palette_left).setOnClickListener() {
            MainActivity.colorClass.Decrease_SpreadID()

            Bitmap_ColorSpread.mNewColors = true
        }

        view.findViewById<Button>(R.id.palette_right).setOnClickListener() {
            MainActivity.colorClass.Increase_SpreadID()

            Bitmap_ColorSpread.mNewColors = true
        }
    }

    fun startFormula(){
        square = SquareValues(MainActivity.rand.nextInt(until = 3))

        pixelData.clearData()

        maxCount = 5000

        doingCalc = true

        coroutineScope.launch(Dispatchers.Default) {
            runFormula()
        }
    }

}