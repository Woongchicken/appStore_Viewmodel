package com.example.appstore.ViewModel

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.Retrofit2.ApiObject
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB

class MainViewModel(application: Application) : AndroidViewModel(application)  {

    val roomDatabase: RoomDB by lazy {
        RoomDB.getInstance(application.applicationContext)
    }
    val mainDao: MainDao by lazy {
        roomDatabase.mainDao()
    }

    // 내부에서 설정하는 자료형은 뮤터블로 변경가능하도록 설정
    private val _resultList = MutableLiveData<List<ApiResult>>()
    private val _result = MutableLiveData<ApiResult>()

    // 변경되지 않는 데이터를 가져 올때 이름은 _ 언더스코어 없이 설정
    // 공개적으로 가져오는 변수는 private이 아닌 퍼블릭으로 외부에서도 접근 가능하도록 설정
    // 하지만 값을 직접 라이브데이터에 접근하지 않고 뷰모델을 통해 가져올수 있도록 설정
    val resultList: LiveData<List<ApiResult>> = _resultList
    val result: LiveData<ApiResult> = _result

    fun setResult(result: ApiResult) {
        _result.value = result
    }

    // 초기 값 설정
//    init {
//        _resultList.value = emptyList()
//    }



    /** API 호출 */
    fun callApi(searchTerm: String) {
        _resultList.postValue(emptyList()) // 초기화
        val call = ApiObject.getRetrofitService.getApp(searchTerm)
        try {
            _resultList.postValue(emptyList()) // 초기화
            val response = call.execute()
            val results = response.body()?.results

            if (results.isNullOrEmpty()) {
                Log.d("Utils.callAPI","API호출 실패 / isNullOrEmpty : ${results}")
            } else {
                Log.d("Utils.callAPI","API호출 성공 / results : ${results}")
                val resultList = results.map { result ->
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
                _resultList.postValue(resultList)
            }
            Log.d("Utils.callAPI","API 호출 실패 / emptyList() ")
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
        Log.d("마지막 검색 목록","Utils - callApi(3) [${Thread.currentThread().name}]")
    }

}


