package com.example.appstore

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.Adapter.SearchAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB
import com.example.appstore.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime : Long = 0    // Enter 키 입력 시간 저장 변수

    private lateinit var searchAdapter: SearchAdapter
    lateinit var model: MainViewModel


    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 뷰 모델 프로바이더를 통해 뷰모델 가져오기
        // 라이프사이클을 가지고 있는 녀석을 넣어줌. 즉 자기 자신
        // 우리가 가져오고 싶은 뷰모델 클래스를 넣어서 뷰모델을 가져오기
        model = ViewModelProvider(this).get(MainViewModel::class.java)

        /** 검색 결과 Adapter 세팅 */
        binding.searchRecyclerView.apply {
            if (!(model.resultList.value.isNullOrEmpty())) {
                binding.searchRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                binding.searchRecyclerView.adapter = searchAdapter
            }
        }

        // 뷰모델이 가지고 있는 값의 변경사항을 관찰할 수 있는 라이브 데이터를 옵저빙한다
        model.resultList.observe(this, Observer {
            Log.d("무한 스크롤", "SearchActivity - mainViewModel - resultList 라이브 데이터 값 변경 : $it")

            // 초기화되지 않은 경우에만 어댑터 초기화
            if (!::searchAdapter.isInitialized) {
                searchAdapter = SearchAdapter() // SearchAdapter 초기화
            }

            searchAdapter.setList(it)
            searchAdapter.notifyItemRangeInserted(0, 10)
        })

        binding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d("무한 스크롤", "MainActivity - addOnScrollListener - onScrolled")
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount-1

                if(!binding.searchRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    searchAdapter.deleteLoading()
                }
            }
        })



        // Serializable로 전달된 ApiResult리스트를 Intent에서 받아옴.
        //val resultList = intent.getSerializableExtra("resultList") as List<ApiResult>

        // Parcelable로 전달된 ApiResult리스트를 Intent에서 받아옴.
        //val resultList = intent.parcelableArrayList<ApiResult>("resultList") as List<ApiResult>

        setInit()   // 초기 셋팅

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭 이벤트 처리 */
        binding.searchButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {    // 클릭한 시간 차를 계산
                val searchTerm = binding.autoCompleteTextView.text.toString()
                Utils.requestSearch(this, searchTerm, mainDao, model) // 검색
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
                    Utils.requestSearch(this, searchTerm, mainDao, model) // 검색

                    mLastEnterTime = SystemClock.elapsedRealtime()
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }

    }

    /** 초기 세팅 */
    private fun setInit() {
        searchTermAuto()                    // 검색어 자동 완성
        setVisibility()           // 검색 결과에 따라 레이아웃 가시성 설정
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto(){
        val adapter = Utils.searchTermAuto(this, mainDao)
        binding.autoCompleteTextView.setAdapter(adapter)
    }
    /** 검색 결과에 따라 레이아웃 가시성 설정 */
    private fun setVisibility() {
        if (model.resultList.value.isNullOrEmpty()) {   // 검색 결과가 없을 경우
            Log.d("검색 결과 테스트", "setVisibility(1) - ${Utils.resultList}")
            binding.saerchResult.visibility = View.VISIBLE
            binding.bottomLinearLayout.visibility = View.GONE
        } else {    // 검색 결과가 있을 경우
            Log.d("검색 결과 테스트", "setVisibility(2) - ${Utils.resultList}")
            binding.saerchResult.visibility = View.GONE
            binding.bottomLinearLayout.visibility = View.VISIBLE
            //setSearchAdapter()        // 검색 결과 Adapter 세팅
        }
    }

    /** 검색 결과 Adapter 세팅 */
//    private fun setSearchAdapter(){
//        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.searchRecyclerView.adapter = SearchAdapter(Utils.resultList!!)
//    }

}