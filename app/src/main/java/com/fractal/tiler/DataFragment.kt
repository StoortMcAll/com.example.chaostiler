package com.fractal.tiler

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.databinding.FragmentDataBinding


class DataFragment : Fragment() {

    private var _fragmentDataBinding : FragmentDataBinding? = null
    private val binding get() = _fragmentDataBinding!!

    var filterId = filter.ordinal


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ((this.activity as AppCompatActivity).supportActionBar?.isShowing == false)
            (this.activity as AppCompatActivity).supportActionBar?.show()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_TabbedFragment_to_FirstFragment)
        }
        callback.isEnabled
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _fragmentDataBinding = FragmentDataBinding.inflate(inflater, container, false)
/*

        val gradient = GradientDrawable()

        gradient.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        gradient.shape= GradientDrawable.RECTANGLE
        gradient.colors = intArrayOf(Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.GREEN)
        gradient.cornerRadius = 8.0F
        gradient.setStroke(2, resources.getColor(R.color.button_stroke))

        val button : Button = binding.undoChanges
        button.foreground =  gradient
*/

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val filters = resources.getStringArray(R.array.filter_types)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_filter, filters)

        binding.autotextview.setAdapter(arrayAdapter)

        val text = arrayAdapter.getItem(filterId).toString()

        binding.autotextview.setText(text, false)

        binding.autotextview.setListSelection(filterId)

     /*   binding.autotextview.onItemClickListener = object :
            AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long, ) {
            }
        }
        */
        binding.autotextview.setOnItemClickListener {
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long ->
                filter = MainActivity.Companion.ImageFilter.values()[position]

                filterId = position
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentDataBinding = null
    }
}

