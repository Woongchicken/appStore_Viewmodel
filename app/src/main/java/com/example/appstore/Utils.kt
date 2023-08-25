package com.example.appstore

import android.R
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Room.MainDao
import com.example.appstore.ViewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

object Utils {
    /** 검색어 자동 완성 */
    fun searchTermAuto(context: Context, mainDao: MainDao) : ArrayAdapter<String> {
        val historyEntityList = mainDao.getHistoryAll()
        val searchTerms: List<String> = historyEntityList.mapNotNull { it.searchTerm }
        val adapter = ArrayAdapter( context, R.layout.simple_dropdown_item_1line, searchTerms)
        return adapter
    }

    /** 검색 버튼을 눌렀을 때 (실제 검색 행위를 수행) */
    suspend fun requestSearch(context: Context, searchTerm: String, model: MainViewModel)  {
        withContext(Dispatchers.IO){
            model.callApi(searchTerm)
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            /* 검색어 히스토리에 기록 */
            if (!(searchTerm.trim().isNullOrEmpty()) && !(model.resultList.value.isNullOrEmpty())
            ) { // 1. 검색어가 없을 경우(공백) 2. 검색 결과가 없을 경우  -> 검색어 히스토리에 안 남김.
                val history = HistoryEntity(searchTerm, formatter.format(date))
                model.mainDao.setInsertHistory(history)
            }
        }
    }

    /** 최근 검색 결과 반환  */
    suspend fun setRecomend(model: MainViewModel) {
        withContext(Dispatchers.IO) {
            val history: HistoryEntity? = model.mainDao.getHistoryRecent()
            var searchTerm = history?.searchTerm ?: "apple" // 최근 검색어가 없으면 검색어 'apple'로 설정

            val firstCall = async { model.callApi(searchTerm) }

            firstCall.await()

            /* 최근 검색어로 검색한 결과, 결과가 없을 경우  */
            if (model.resultList.value.isNullOrEmpty()) {
                Log.d("Test", "Utils - setRecomend(2) // resultList : ${model.resultList.value}")
                searchTerm = "apple"    // 검색어 'apple'로 설정 후 재 검색
                model.callApi(searchTerm)
            }
        }
    }


}


