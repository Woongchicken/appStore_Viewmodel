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
import com.example.appstore.databinding.FragmentArcadeBinding


class ArcadeFragment : Fragment() {
    private lateinit var binding: FragmentArcadeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArcadeBinding.inflate(inflater, container, false)
        binding.fragment = this     // Layout Data Binding
        return binding.root
    }

    /** 탭 내비게이션 */
    fun navigateToTodayFragment(view: View) {
        findNavController().navigate(R.id.action_arcadeFragment_to_todayFragment)
    }

    fun navigateToGameFragment(view: View) {
        findNavController().navigate(R.id.action_arcadeFragment_to_gameFragment)
    }

    fun navigateToAppFragment(view: View) {
        findNavController().navigate(R.id.action_arcadeFragment_to_appFragment)
    }

    fun navigateToMainFragment(view: View) {
        findNavController().navigate(R.id.action_arcadeFragment_to_mainFragment)
    }

}