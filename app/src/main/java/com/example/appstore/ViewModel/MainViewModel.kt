package com.example.appstore.ViewModel

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstore.Adapter.RecomendAdapter
import com.example.appstore.Retrofit2.ApiObject
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Integer.min

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val roomDatabase: RoomDB by lazy {
        RoomDB.getInstance(application.applicationContext)
    }
    val mainDao: MainDao by lazy {
        roomDatabase.mainDao()
    }

    // 내부에서 설정하는 자료형은 뮤터블로 변경가능하도록 설정
    private val _resultList = MutableLiveData<List<ApiResult>>()
    private val _recomendList = MutableLiveData<List<ApiResult>>()
    private val _searchList = MutableLiveData<List<ApiResult>>()
    private val _result = MutableLiveData<ApiResult>()

    // 공개적으로 가져오는 변수는 private이 아닌 퍼블릭으로 외부에서도 접근 가능하도록 설정
    val resultList: LiveData<List<ApiResult>> = _resultList
    val recomendList: LiveData<List<ApiResult>> = _recomendList
    val searchList: LiveData<List<ApiResult>> = _searchList
    val result: LiveData<ApiResult> = _result

    // lieveData의 특정 List 초기화
    fun clearTypeList(typeList: String) {
        when(typeList) {
            "searchList" -> _searchList.value = emptyList()
            else -> _recomendList.value = emptyList()
        }
    }

    /**  목록 10개씩 옮기기 (resultList -> typeList) */
    fun moveTypeList(typeList: String, startPosition: Int, endPosition: Int) {
        val currentResultList = _resultList.value
        if (currentResultList != null) {
            val endIndex = min(endPosition, currentResultList.size)
            if (startPosition >= 0 && startPosition < endIndex) {
                val sublist = currentResultList.subList(startPosition, endIndex)    // 전체 결과 값에서 인덱스에 해당하는 부분만 갖고옴.
                when(typeList) {
                    "searchList" -> _searchList.value = sublist
                    else -> _recomendList.value = sublist
                }
            }
        }
    }

    fun setResult(result: ApiResult) {
        _result.value = result
    }

    /** API 호출 */
    suspend fun callApi(searchTerm: String) {
        val call = ApiObject.getRetrofitService.getApp(searchTerm)
        try {
            val response = call.execute()
            val results = response.body()?.results
            if (results.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    _resultList.value = emptyList()     // 검색 결과 없을 경우 - 빈 값으로 초기화
                }
            } else {
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
                withContext(Dispatchers.Main) {
                    _resultList.value = resultList
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }
}


