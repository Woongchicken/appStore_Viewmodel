package com.example.appstore.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.R
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ActivityMainBinding
import com.example.appstore.databinding.FragmentMainBinding


/*

9/11 commit
1. ViewModel & LiveData 적용 (검색 결과만, 스크린샷 X)
2. call API - postValue -> setValue
3. HistoryAdapter - 중복 방지 변수 적용
4. Glide 이미지 캐싱 (아이콘, 스크린샷)
5. 로그인 & 회원가입
6. 회원별 History 저장, 불러오기 (FireBase-RealDataBase) // RoomDB 사용 X
7. Button Visible -> viewTreeObserver로 변경
8. 탭 구현 (Layout-DataBinding)

*할 일
1. 회원 가입 필드추가 및 예외 추가

*/

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}