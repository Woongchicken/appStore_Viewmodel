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
    fun requestSearch(context: Context, searchTerm: String, mainDao: MainDao, model: MainViewModel)  {
        CoroutineScope(Dispatchers.Main).launch {
            searchApp(searchTerm, mainDao, model)    // api 호출하여 검색 결과 얻음.
            // startSearchActivity(context)          // 검색 결과 페이지로 이동
        }
    }

    /** 검색 기능 (검색 결과 반환만 필요한 경우) */
    suspend fun searchApp(searchTerm: String, mainDao: MainDao, model: MainViewModel) = withContext(Dispatchers.IO) {
        model.callApi(searchTerm)

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        /* 검색어 히스토리에 기록 */
        if (!(searchTerm.trim().isNullOrEmpty()) && !(model.resultList.value.isNullOrEmpty())
        ) { // 1. 검색어가 없을 경우(공백) 2. 검색 결과가 없을 경우  -> 검색어 히스토리에 안 남김.
            val history = HistoryEntity(searchTerm, formatter.format(date))
            mainDao.setInsertHistory(history)
        }
    }


    /** 최근 검색 결과 반환  */
    suspend fun setRecomend(mainDao: MainDao, model: MainViewModel) = withContext(Dispatchers.IO) {
        val history: HistoryEntity? = mainDao.getHistoryRecent()
        var searchTerm = history?.searchTerm ?: "apple" // 최근 검색어가 없으면 검색어 'apple'로 설정

        Log.d("마지막 검색 목록","Utils - setRecomend(1) [${Thread.currentThread().name}]")
        model.callApi(searchTerm)
        Log.d("마지막 검색 목록","Utils - setRecomend(2) [${Thread.currentThread().name}]")

        /* 최근 검색어로 검색한 결과, 결과가 없을 경우  */
        if (model.resultList.value.isNullOrEmpty()) {
            searchTerm = "apple"    // 검색어 'apple'로 설정 후 재 검색
            model.callApi(searchTerm)
            Log.d("마지막 검색 목록","Utils - setRecomend(3) [${Thread.currentThread().name}]")
        }
    }


    /** DetailActivity로 이동 (Data1:result-단일 검색 결과) */
//    fun startDetailActivity(context: Context, result : ApiResult){
//        val intent = Intent(context, DetailActivity::class.java)
////        intent.putExtra("result", result as Serializable)
//        intent.putExtra("result", result as Parcelable)
//        context.startActivity(intent)
//    }

    /** SearchActivity로 이동 (Data1:resultList-멀티 검색 결과) */
//    fun startSearchActivity(context: Context) {
//        val intent = Intent(context,SearchActivity::class.java)
////        intent.putExtra("resultList", resultList as Serializable)
////        intent.putExtra("resultList", resultList as ArrayList<Parcelable>)
//        context.startActivity(intent)
//    }

}


