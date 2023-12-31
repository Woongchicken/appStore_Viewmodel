package com.example.appstore

import android.R
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.appstore.Adapter.SearchAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Room.MainDao
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.main.LoadingFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

object Utils {
    private var loadingFragment: LoadingFragment? = null
    private var auth =  Firebase.auth
    private val database = Firebase.database
    private val myAppStoreRef = database.getReference("appStore_ref").child(auth.currentUser!!.uid)
    private val historyModelList: MutableList<HistoryModel> = mutableListOf()

    /** 검색어 자동 완성 */
    fun searchTermAuto(context: Context) : ArrayAdapter<String> {
        // val historyEntityList = mainDao.getHistoryAll()  // Room DB

        // History 불러오기 (Realtime Database 불러오기)
        myAppStoreRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    historyModelList.clear()
                    for (dataModel in snapshot.children) {
                        historyModelList.add(dataModel.getValue(HistoryModel::class.java)!!)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AppStore","dbError")
                }
            })

        val searchTerms: List<String> = historyModelList.mapNotNull { it.searchTerm }
        val adapter = ArrayAdapter( context, R.layout.simple_dropdown_item_1line, searchTerms)
        return adapter
    }

    /** 검색 버튼을 눌렀을 때 (실제 검색 행위를 수행) */
    suspend fun requestSearch(searchTerm: String, model: MainViewModel)  {
        withContext(Dispatchers.IO){
            model.callApi(searchTerm)
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            /* 검색어 히스토리에 기록 */
            if (!(searchTerm.trim().isNullOrEmpty()) && !(model.resultList.value.isNullOrEmpty())
            ) { // 1. 검색어가 없을 경우(공백) 2. 검색 결과가 없을 경우  -> 검색어 히스토리에 안 남김.
                val history = HistoryEntity(searchTerm, formatter.format(date))
                val historyModel = HistoryModel(searchTerm, formatter.format(date))
                // model.mainDao.setInsertHistory(history)      // Room DB

                // 검색에 저장 (Realtime Database 저장하기)
                myAppStoreRef
                    .child(searchTerm)
                    //.push()                     // 다중 데이터 삽입 가능
                    .setValue(historyModel)
            }
        }
    }

    /** 최근 검색 결과 반환  */
    suspend fun setRecomend(model: MainViewModel, historyModelList: MutableList<HistoryModel>) = withContext(Dispatchers.IO) {
//            val history: HistoryEntity? = model.mainDao.getHistoryRecent()    // Room DB
//            var searchTerm = history?.searchTerm ?: "apple" // 최근 검색어가 없으면 검색어 'apple'로 설정
        var searchTerm = "apple"
        historyModelList.sortByDescending { it.date }               // 'date' 필드를 기준으로 내림차순 정렬
        if(historyModelList.size != 0){
            searchTerm = historyModelList[0].searchTerm
        }

        model.callApi(searchTerm)

        /* 최근 검색어로 검색한 결과, 결과가 없을 경우  */
        if (model.resultList.value.isNullOrEmpty()) {
            searchTerm = "apple"    // 검색어 'apple'로 설정 후 재 검색
            model.callApi(searchTerm)
        }
    }

    /** 로딩화면 표시 */
    fun showLoadingFragment(fragment : Fragment) {
        if (loadingFragment == null) {
            loadingFragment = LoadingFragment()
            loadingFragment?.isCancelable = false // 로딩 중에는 취소할 수 없도록 설정
        }
        loadingFragment?.show(fragment.parentFragmentManager, "loading")
    }

    /** 로딩화면 숨김 */
    fun hideLoadingFragment() {
        loadingFragment?.dismiss()      //  프래그먼트를 닫고 메모리에서 제거
        loadingFragment = null
    }

    /** Glide - 이미지 캐싱 */
    fun preload(context: Context, url : String) {
        Glide.with(context).load(url)
            .preload(500, 500)  // 지정된 크기로 조정하여 캐싱
    }

}


