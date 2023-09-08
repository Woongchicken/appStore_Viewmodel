package com.example.appstore.Login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.appstore.R
import com.example.appstore.databinding.FragmentSplashBinding
import com.example.appstore.main.MainActivity
import com.example.appstore.main.MainFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashFragment : Fragment() {

    private lateinit var binding : FragmentSplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        if(auth.currentUser?.uid == null) {
            // 회원가입이 안되어있으면 -> JoinFragment
            Handler().postDelayed({
                findNavController().navigate(R.id.action_splashFragment_to_joinFragment)
            },500)
        } else {
            // 회원가입이 되어있으면 -> MainFragment
            Handler().postDelayed({
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            },500)
        }

        return binding.root
    }

}