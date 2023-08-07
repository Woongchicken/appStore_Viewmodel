package com.example.appstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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


/*

8/7 commit

* 할 일
1. isSearching 안되는 이유 찾기
2. 페이지 로딩 느림 -> 페이지 갯수 제한


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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setInit()   // 초기 셋팅

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭하여 검색 */
        binding.searchButton.setOnClickListener {
            val searchTerm = binding.autoCompleteTextView.text.toString()
            Utils.requestSearch(this,searchTerm,mainDao) // 검색
        }

        /* Enter 키 입력 이벤트 처리 */
        binding.autoCompleteTextView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val searchTerm = binding.autoCompleteTextView.text.toString()
                Utils.requestSearch(this,searchTerm,mainDao) // 검색
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    override fun onResume() {
        super.onResume()
        setInit()   // 초기 셋팅
    }

    /** 초기 세팅 */
    private fun setInit() {
        Utils.countSearching = 0            // 중복 검색 방지 변수 초기화
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