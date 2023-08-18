package com.example.appstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ActivityMainBinding


/*

8/18 commit (중복클릭 방지)

viewModel 수정 중
* 할 일
1. 페이지 로딩 -> Infinite Scroll
2. 탭 나누기
3. viewModel, 코루틴 생명주기 맞추기. (검색 결과 불일치)
*/

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var model: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // model = ViewModelProvider(this)[MainViewModel::class.java]
    }

}