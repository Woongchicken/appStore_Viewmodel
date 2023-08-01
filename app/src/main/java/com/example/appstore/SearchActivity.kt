package com.example.appstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appstore.Adapter.SearchAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB
import com.example.appstore.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
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

        val resultList = intent.getSerializableExtra("resultList") as List<ApiResult>

        setInit(resultList)   // 초기 셋팅

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

    /** 초기 세팅 */
    private fun setInit(resultList : List<ApiResult>) {
        Utils.countSearching = 0            // 중복 검색 방지 변수 초기화
        searchTermAuto()                    // 검색어 자동 완성
        setVisibility(resultList)           // 검색 결과에 따라 레이아웃 가시성 설정
        setSearchAdapter(resultList)        // 검색 결과 Adapter 세팅
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto(){
        val adapter = Utils.searchTermAuto(this, mainDao)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /** 검색 결과에 따라 레이아웃 가시성 설정 */
    private fun setVisibility(resultList : List<ApiResult>) {
        if (resultList.isNullOrEmpty()) {   // 검색 결과가 없을 경우
            binding.saerchResult.visibility = View.VISIBLE
            binding.bottomLinearLayout.visibility = View.GONE
        } else {                            // 검색 결과가 있을 경우
            binding.saerchResult.visibility = View.GONE
            binding.bottomLinearLayout.visibility = View.VISIBLE
        }
    }

    /** 검색 결과 Adapter 세팅 */
    private fun setSearchAdapter(resultList: List<ApiResult>){
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = SearchAdapter(resultList)
    }

}