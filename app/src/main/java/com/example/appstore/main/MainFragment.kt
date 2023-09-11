package com.example.appstore.main

import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.Adapter.HistoryAdapter
import com.example.appstore.Adapter.RecomendAdapter
import com.example.appstore.HistoryModel
import com.example.appstore.R
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Utils


import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private var mLastClickTime: Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime: Long = 0    // Enter 키 입력 시간 저장 변수

    private var page = 0       // API 호출 결과 페이지
    private var startPosition = 1
    private var endPosition = 1

    private lateinit var recomendAdapter: RecomendAdapter
    lateinit var model: MainViewModel
    private val mainDao by lazy {
        model.mainDao
    }

    private var auth = Firebase.auth
    private val database = Firebase.database
    private val myAppStoreRef = database.getReference("appStore_ref").child(auth.currentUser!!.uid)
    private val historyModelList: MutableList<HistoryModel> = mutableListOf()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.fragment = this     // Layout Data Binding

        page = 0       // API 호출 결과 페이지
        // 뷰 모델 프로바이더를 통해 뷰모델 가져오기
        model = ViewModelProvider(requireActivity())[MainViewModel::class.java] // requireActivity() - 현재 Fragment가 속한 Activity의 참조를 반환

        setInit()   // 초기 셋팅

        // 로그아웃 버튼 클릭
        binding.userIcon.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "로그아웃 성공", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_mainFragment_to_joinFragment)
        }

        // recomendList 라이브 데이터를 옵저빙
        model.recomendList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                // recomendAdapter가 초기화 안 되어있으면 -> 초기화
                if (!::recomendAdapter.isInitialized) {     // ::.isInitialized -> lateinit 초기화 여부 확인
                    recomendAdapter = RecomendAdapter(model)  // RecomendAdapter 초기화
                }
                recomendAdapter.setList(it)
                recomendAdapter.notifyItemRangeInserted(startPosition, endPosition)
            }
        })

        //  사용자의 스크롤을 감지하는 리스너
        binding.recomendRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1

                // 스크롤 끝에 도달했는지 체크
                if (!binding.recomendRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) { // 1. canScrollVertically - 수직스크롤이 끝에 도달했는지 알려주는 메서드, 최상단에 도달 : -1, 최하단에 도달 : 1  // && 2. 데이터의 마지막 아이템이 화면에 뿌려졌는지
                    loadMoreData()  // 스크롤이 마지막 아이템에 도달하면 추가 데이터를 로드
                }
            }
        })

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭 이벤트 처리 */
        binding.searchButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) { // 클릭한 시간 차를 계산
                val currentFragment = this
                mainScope.launch {
                    Utils.showLoadingFragment(currentFragment)     // 로딩 화면 표시
                    val searchTerm = binding.autoCompleteTextView.text.toString()
                    Utils.requestSearch(searchTerm, model) // 검색
                    it.findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                    mLastClickTime = SystemClock.elapsedRealtime()  // elapsedRealtime() - 안드로이드 시스템 시간을 나타내는 함수, 시스템 부팅 이후로 경과한 시간(밀리초)을 반환
                    Utils.hideLoadingFragment()     // 작업이 완료되면 로딩 화면 숨김
                }
            }
        }

        /* Enter 키 입력 이벤트 처리 */
        binding.autoCompleteTextView.inputType = EditorInfo.TYPE_CLASS_TEXT
        binding.autoCompleteTextView.maxLines = 1  // multiple line 제거
        binding.autoCompleteTextView.imeOptions = EditorInfo.IME_ACTION_SEARCH // Enter키 대신 돋보기가 나타남.

        binding.autoCompleteTextView.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {                   // 소프트 키보드의 "검색" 버튼이나 하드웨어 키보드의 Enter 키를 눌렀을 때
                if (SystemClock.elapsedRealtime() - mLastEnterTime > 5000) {  // Enter 키 입력한 시간 차를 계산
                    val currentFragment = this
                    mainScope.launch {
                        Utils.showLoadingFragment(currentFragment)     // 로딩 화면 표시
                        val searchTerm = binding.autoCompleteTextView.text.toString()
                        Utils.requestSearch(searchTerm, model) // 검색
                        findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                        mLastEnterTime = SystemClock.elapsedRealtime()
                        Utils.hideLoadingFragment()     // 작업이 완료되면 로딩 화면 숨김
                    }
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }
        return binding.root
    }

    /** 초기 세팅 */
    private fun setInit() {
        val currentFragment = this
        mainScope.launch {
            Utils.showLoadingFragment(currentFragment)     // 로딩 화면 표시
            searchTermAuto()                    // 검색어 자동 완성
            setHistoryAdapter()            // 최근 검색어
            setRecomendAdapter()                // 마지막 검색 목록
            Utils.hideLoadingFragment()     // 작업이 완료되면 로딩 화면 숨김
        }
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto() {
        val adapter = Utils.searchTermAuto(binding.root.context)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /** 최근 검색어  Adapter 세팅 */
    suspend fun setHistoryAdapter() {
        val currentFragment = this

        //val historyEntityList = mainDao.getHistoryAll()       // Room DB

        loadHistoryData()   // History 불러오기

        // 'date' 필드를 기준으로 내림차순 정렬
        historyModelList.sortByDescending { it.date }

        binding.newLookRecyclerView.itemAnimator = null         //  애니메이션 효과 제거
        binding.newLookRecyclerView.layoutManager = GridLayoutManager(binding.root.context, 2)     //  spanCount - 그리드의 열 수를 나타내는 정수 값
        binding.newLookRecyclerView.adapter = HistoryAdapter(historyModelList, model, mainScope, currentFragment)
    }

    /** Realtime Database에서 History 불러오기 */
    suspend fun loadHistoryData() {
        suspendCancellableCoroutine<Unit> { continuation ->
            myAppStoreRef
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        historyModelList.clear()
                        for (dataModel in snapshot.children) {
                            historyModelList.add(dataModel.getValue(HistoryModel::class.java)!!)
                        }
                        continuation.resume(Unit, null)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AppStore", "dbError")
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }


    /** 마지막 검색 목록 Adapter 세팅 */
    suspend fun setRecomendAdapter() {
        model.clearTypeList("recomendList")      // recomendList 초기화

        if (!::recomendAdapter.isInitialized) {
            recomendAdapter = RecomendAdapter(model)  // RecomendAdapter 초기화
        }

        recomendAdapter.setClear()      // RecyclerView List 초기

        Utils.setRecomend(model, historyModelList)       // api 호출

        loadMoreData()      // 인덱스 증가

        binding.recomendRecyclerView.itemAnimator = null    //  애니메이션 효과 제거
        binding.recomendRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        binding.recomendRecyclerView.adapter = recomendAdapter
    }


    /** 마지막 검색 목록 - 추가 데이터 로드 */
    private fun loadMoreData() {
        ++page
        startPosition = (page - 1) * 10
        endPosition = startPosition + 10

        model.moveTypeList("recomendList", startPosition, endPosition)     // API 검색 결과(ResultList) -> 마지막 검색 목록 결과(RecomendList) 10개씩 옮기기
    }


    /** 탭 내비게이션 */
    fun navigateToTodayFragment(view: View) {
        findNavController().navigate(R.id.action_mainFragment_to_todayFragment)
    }

    fun navigateToGameFragment(view: View) {
        findNavController().navigate(R.id.action_mainFragment_to_gameFragment)
    }

    fun navigateToAppFragment(view: View) {
        findNavController().navigate(R.id.action_mainFragment_to_appFragment)
    }

    fun navigateToArcadeFragment(view: View) {
        findNavController().navigate(R.id.action_mainFragment_to_arcadeFragment)
    }
}