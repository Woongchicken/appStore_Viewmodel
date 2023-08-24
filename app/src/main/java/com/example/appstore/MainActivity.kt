package com.example.appstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ActivityMainBinding


/*

8/24 commit
ViewModel 적용 (스크린샷 제외)

ScreenShotAdapter -> ScreenShotAdapter
DetailFragment -> ScreenShotAdapter

탭 구현
*/

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}