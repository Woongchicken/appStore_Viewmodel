package com.example.appstore.sub

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.appstore.R
import com.example.appstore.databinding.FragmentArcadeBinding
import com.example.appstore.databinding.FragmentTodayBinding

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        binding.fragment = this     // Layout Data Binding
        setInit()
        return binding.root
    }

    private fun setInit() {
        setTabColor()
    }

    /** 탭 컬러설정 */
    private fun setTabColor() {
        binding.todayImg.setColorFilter(ContextCompat.getColor(requireContext(), R.color.appstore_primary))
        binding.todayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.appstore_primary))
    }


    /** 탭 내비게이션 */
    fun navigateToGameFragment(view: View) {
        findNavController().navigate(R.id.action_todayFragment_to_gameFragment)
    }

    fun navigateToAppFragment(view: View) {
        findNavController().navigate(R.id.action_todayFragment_to_appFragment)
    }

    fun navigateToArcadeFragment(view: View) {
        findNavController().navigate(R.id.action_todayFragment_to_arcadeFragment)
    }

    fun navigateToMainFragment(view: View) {
        findNavController().navigate(R.id.action_todayFragment_to_mainFragment)
    }
}