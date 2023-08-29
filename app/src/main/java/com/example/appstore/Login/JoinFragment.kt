package com.example.appstore.Login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.appstore.databinding.FragmentJoinBinding
import com.example.appstore.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinFragment : Fragment() {

    private lateinit var binding : FragmentJoinBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        // 회원가입 버튼 클릭
        binding.joinBtn.setOnClickListener {
            val email = binding.emailArea.text.toString()
            val password = binding.passwordArea.text.toString()

            /** 이메일 회원가입 */
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // 회원 가입 성공 -> MainActivity
                        Toast.makeText(requireContext(),"회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(),"회원가입 실패", Toast.LENGTH_SHORT).show()
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
        }


        // 로그인 버튼 클릭
        binding.loginBtn.setOnClickListener {
            val email = binding.emailArea.text.toString()
            val password = binding.passwordArea.text.toString()

            /** 이메일 로그인 */
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공 -> MainActivity
                        Toast.makeText(requireContext(),"로그인 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(),"로그인 실패", Toast.LENGTH_SHORT).show()
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
        }

        return binding.root
    }

}