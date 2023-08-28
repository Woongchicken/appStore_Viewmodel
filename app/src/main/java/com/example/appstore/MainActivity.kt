package com.example.appstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ActivityMainBinding


/*

8/28 commit
ViewModel & LiveData 적용 (검색 결과만, 스크린샷 X)
call API - postValue -> setValue
HistoryAdapter - 중복 방지 변수 적용

*할 일
1. 로그인
2. 탭 구현
3. 스크린샷 -> 이미지 처리 (Glide대신 다른 메서드, 데이터 캐싱)
4. 구조적으로 잘못

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