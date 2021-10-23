package com.fractal.tiler

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.fractal.tiler.MainActivity.Companion.filter
import com.fractal.tiler.databinding.FragmentDataBinding


class DataFragment : Fragment() {

    private var _fragmentDataBinding : FragmentDataBinding? = null
    private val binding get() = _fragmentDataBinding!!

    var filterId = filter.ordinal

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _fragmentDataBinding = FragmentDataBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val filters = resources.getStringArray(R.array.filter_types)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_filter, filters)

        binding.autotextview.setAdapter(arrayAdapter)

        val text = arrayAdapter.getItem(filterId).toString()

        binding.autotextview.setText(text, false)

        val id = binding.autotextview.getListSelection()

        binding.autotextview.setListSelection(filterId)

     /*   binding.autotextview.onItemClickListener = object :
            AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long, ) {
                TODO("Not yet implemented")
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
