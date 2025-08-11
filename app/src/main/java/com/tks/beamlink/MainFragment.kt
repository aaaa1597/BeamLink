package com.tks.beamlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tks.beamlink.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var _binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return _binding.root
    }

    companion object {
    }
}