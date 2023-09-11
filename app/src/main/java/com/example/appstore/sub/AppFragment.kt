package com.example.appstore.sub

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.appstore.R
import com.example.appstore.databinding.FragmentAppBinding

class AppFragment : Fragment() {
    private lateinit var binding: FragmentAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppBinding.inflate(inflater, container, false)
        binding.fragment = this     // Layout Data Binding
        return binding.root
    }

    /** 탭 내비게이션 */
    fun navigateToTodayFragment(view: View) {
        findNavController().navigate(R.id.action_appFragment_to_todayFragment)
    }

    fun navigateToGameFragment(view: View) {
        findNavController().navigate(R.id.action_appFragment_to_gameFragment)
    }

    fun navigateToArcadeFragment(view: View) {
        findNavController().navigate(R.id.action_appFragment_to_arcadeFragment)
    }

    fun navigateToMainFragment(view: View) {
        findNavController().navigate(R.id.action_appFragment_to_mainFragment)
    }
}