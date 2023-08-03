package com.example.appstore

import android.R
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import com.example.appstore.Retrofit2.ApiObject
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Room.MainDao
import com.google.android.material.internal.ParcelableSparseArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

object Utils {
    var countSearching : Int = 0        // 중복 검색 방지 변수
    var resultList: List<ApiResult>? = null       // 검색 결과 리스트


    /** 검색어 자동 완성 */
    fun searchTermAuto(context: Context, mainDao: MainDao) : ArrayAdapter<String> {
        val historyEntityList = mainDao.getHistoryAll()
        val searchTerms: List<String> = historyEntityList.mapNotNull { it.searchTerm }
        val adapter = ArrayAdapter( context, R.layout.simple_dropdown_item_1line, searchTerms)
        return adapter
    }

    /** 검색 버튼을 눌렀을 때 (실제 검색 행위를 수행) */
    fun requestSearch(context: Context, searchTerm: String, mainDao: MainDao) {
        if (countSearching == 0) {       // 검색버튼 중복으로 눌렀는지 체크
            countSearching++
            searchApp(searchTerm,mainDao)    // api 호출하여 검색 결과 얻음.
            startSearchActivity(context)          // 검색 결과 페이지로 이동
        }
    }

    /** 검색 기능 (검색 결과 반환만 필요한 경우) */
    fun searchApp(searchTerm: String, mainDao: MainDao) = runBlocking{
        async{
            callApi(searchTerm)
        }.await()

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        /* 검색어 히스토리에 기록 */
        if (!(searchTerm.trim().isNullOrEmpty()) && !(resultList.isNullOrEmpty())){ // 1. 검색어가 없을 경우(공백) 2. 검색 결과가 없을 경우  -> 검색어 히스토리에 안 남김.
            val history = HistoryEntity(searchTerm, formatter.format(date))
            mainDao.setInsertHistory(history)
        }
    }

    /** API 호출 */
    private suspend fun callApi(searchTerm: String) = withContext(Dispatchers.IO) {
        val call = ApiObject.getRetrofitService.getApp(searchTerm)
        try {
            val response = call.execute()
            val results = response.body()?.results

            if (results.isNullOrEmpty()) {
                Log.d("Utils.callAPI","API호출 실패 / isNullOrEmpty : ${results}")
            } else {
                Log.d("Utils.callAPI","API호출 성공 / results : ${results}")
                resultList = results.map { result ->
                    ApiResult(
                        artworkUrl512 = result.artworkUrl512 ?: " ",
                        trackName = result.trackName ?: " ",
                        averageUserRating = result.averageUserRating ?: 0f,
                        screenshotUrls = result.screenshotUrls,
                        description = result.description ?: " ",
                        trackContentRating = result.trackContentRating ?: " ",
                        artistName = result.artistName ?: " ",
                        userRatingCount = result.userRatingCount ?: " ",
                        primaryGenreName = result.primaryGenreName ?: " ",
                        releaseNotes = result.releaseNotes ?: " "
                    )
                }
                Log.d("Utils.callAPI","API호출 성공 / resultList : ${resultList}")
            }
            Log.d("Utils.callAPI","API 호출 실패 / emptyList() ")
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }


    /** 최근 검색 결과 반환  */
    fun setRecomend(mainDao: MainDao) = runBlocking {
        val history: HistoryEntity? = mainDao.getHistoryRecent()
        var searchTerm = history?.searchTerm ?: "apple" // 최근 검색어가 없으면 검색어 'apple'로 설정

        async{
            callApi(searchTerm)
        }.await()

        /* 최근 검색어로 검색한 결과, 결과가 없을 경우  */
        if (resultList.isNullOrEmpty()) {
            searchTerm = "apple"    // 검색어 'apple'로 설정 후 재 검색
            async{
                callApi(searchTerm)
            }.await()
        }

    }


    /** DetailActivity로 이동 (Data1:result-단일 검색 결과) */
    fun startDetailActivity(context: Context, result : ApiResult){
        val intent = Intent(context, DetailActivity::class.java)
//        intent.putExtra("result", result as Serializable)
        intent.putExtra("result", result as Parcelable)
        context.startActivity(intent)
    }

    /** SearchActivity로 이동 (Data1:resultList-멀티 검색 결과) */
    fun startSearchActivity(context: Context) {
        val intent = Intent(context,SearchActivity::class.java)
//        intent.putExtra("resultList", resultList as Serializable)
//        intent.putExtra("resultList", resultList as ArrayList<Parcelable>)
        context.startActivity(intent)



    }

}


