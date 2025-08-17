package com.tks.beamlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tks.beamlink.databinding.FragmentBleBinding

class BleFragment : Fragment() {
    private lateinit var _binding: FragmentBleBinding
    private val _viewModel: BleViewModel by lazy {
        ViewModelProvider(this)[BleViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBleBinding.inflate(inflater, container, false)
        return _binding.root
    }

    companion object {
        fun newInstance() = BleFragment()
    }
}