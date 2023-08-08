package com.example.appstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.sqlite.db.SupportSQLiteCompat.Api16Impl.cancel
import com.example.appstore.Adapter.HistoryAdapter
import com.example.appstore.Adapter.RecomendAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB
import com.example.appstore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.lang.System.currentTimeMillis


/*

8/8 commit (중복클릭 방지)

* 할 일
1. 페이지 로딩 -> Infinite Scroll
2. 탭 나누기
3. fragment로 바꾸기


*/

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val roomDatabase: RoomDB by lazy {
        RoomDB.getInstance(this)
    }
    private val mainDao: MainDao by lazy {
        roomDatabase.mainDao()
    }

    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime : Long = 0    // Enter 키 입력 시간 저장 변수

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setInit()   // 초기 셋팅

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭 이벤트 처리 */
        binding.searchButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {    // 클릭한 시간 차를 계산
                val searchTerm = binding.autoCompleteTextView.text.toString()
                Utils.requestSearch(this, searchTerm, mainDao) // 검색
            }
            mLastClickTime = SystemClock.elapsedRealtime()  // elapsedRealtime() - 안드로이드 시스템 시간을 나타내는 함수, 시스템 부팅 이후로 경과한 시간(밀리초)을 반환
        }

        /* Enter 키 입력 이벤트 처리 */
        binding.autoCompleteTextView.inputType = EditorInfo.TYPE_CLASS_TEXT
        binding.autoCompleteTextView.maxLines=1  // multiple line 제거
        binding.autoCompleteTextView.imeOptions = EditorInfo.IME_ACTION_SEARCH // Enter키 대신 돋보기가 나타남.

        binding.autoCompleteTextView.setOnEditorActionListener { _, actionId, event ->
            // 소프트 키보드의 "검색" 버튼이나 하드웨어 키보드의 Enter 키를 눌렀을 때
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (SystemClock.elapsedRealtime() - mLastEnterTime > 5000) {  // Enter 키 입력한 시간 차를 계산

                    val searchTerm = binding.autoCompleteTextView.text.toString()
                    Utils.requestSearch(this, searchTerm, mainDao) // 검색

                    mLastEnterTime = SystemClock.elapsedRealtime()
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onResume() {
        super.onResume()
        setInit()   // 초기 셋팅
    }

    /** 초기 세팅 */
    private fun setInit() {
        searchTermAuto()                    // 검색어 자동 완성
        setHistoryAdapter()                 // 최근 검색어
        setRecentAdapter()                  // 마지막 검색 목록
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto(){
        val adapter = Utils.searchTermAuto(this, mainDao)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /** 최근 검색어  Adapter 세팅 */
    private fun setHistoryAdapter(){
        val historyEntityList = mainDao.getHistoryAll()

        binding.newLookRecyclerView.layoutManager =
            GridLayoutManager(this, 2)     //  spanCount - 그리드의 열 수를 나타내는 정수 값
        binding.newLookRecyclerView.adapter = HistoryAdapter(historyEntityList, mainDao)
    }

    /** 마지막 검색 목록 Adapter 세팅 */
    private fun setRecentAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("마지막 검색 목록","MainActivity - setRecentAdapter(1) [${Thread.currentThread().name}]")
            Utils.setRecomend(mainDao)
            Log.d("마지막 검색 목록","MainActivity - setRecentAdapter(2) [${Thread.currentThread().name}]")

            if (!(Utils.resultList.isNullOrEmpty())) {
                binding.recomendRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                binding.recomendRecyclerView.adapter = RecomendAdapter(Utils.resultList!!)
            }
        }
    }

}