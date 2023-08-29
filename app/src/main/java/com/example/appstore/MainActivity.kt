package com.example.appstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ActivityMainBinding


/*

8/29 commit
1. ViewModel & LiveData 적용 (검색 결과만, 스크린샷 X)
2. call API - postValue -> setValue
3. HistoryAdapter - 중복 방지 변수 적용
4. Glide 이미지 캐싱 (아이콘, 스크린샷)

*할 일
1. 로그인
2. 탭 구현

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