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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashFragment : Fragment() {

    private lateinit var binding : FragmentSplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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
            },2000)
        } else {
            // 회원가입이 되어있으면 -> MainActivity
            Handler().postDelayed({
                startActivity(Intent(requireContext(), MainActivity::class.java))
            },2000)
        }

        return binding.root
    }

}