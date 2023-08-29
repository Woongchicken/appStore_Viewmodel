package com.example.appstore

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.Adapter.RecomendAdapter
import com.example.appstore.Adapter.SearchAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentMainBinding
import com.example.appstore.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var binding : FragmentSearchBinding
    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime : Long = 0    // Enter 키 입력 시간 저장 변수
    private var page = 0            // 검색 결과 페이지
    private var startPosition = 1
    private var endPosition = 1
    private lateinit var searchAdapter : SearchAdapter
    lateinit var model: MainViewModel
    private val mainDao by lazy {
        model.mainDao
    }

    private val requestSearchScope = CoroutineScope(Dispatchers.Main)   // 검색 코루틴스코프

    override fun onResume() {
        super.onResume()
        setInit()   // 초기 셋팅
    }

    override fun onDestroy() {
        super.onDestroy()
        model.clearTypeList("searchList")      // searchList 초기화
        requestSearchScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        page = 0       // API 호출 결과 페이지
        // 뷰 모델 프로바이더를 통해 뷰모델 가져오기
        model = ViewModelProvider(requireActivity())[MainViewModel::class.java] // requireActivity() - 현재 Fragment가 속한 Activity의 참조를 반환

        setInit()

        setSearchAdapter()    // 검색 결과 Adapter 세팅

        // searchList 라이브 데이터를 옵저빙
        model.searchList.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                searchAdapter.setList(it)
                searchAdapter.notifyItemRangeInserted(startPosition,endPosition)
            }
        })

        //  사용자의 스크롤을 감지하는 리스너
        binding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount-1

                // 스크롤 끝에 도달했는지 체크
                if (!binding.searchRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) { // 1. canScrollVertically - 수직스크롤이 끝에 도달했는지 알려주는 메서드, 최상단에 도달 : -1, 최하단에 도달 : 1 && 2. 데이터의 마지막 아이템이 화면에 뿌려졌는지
                    loadMoreData()    // 스크롤이 마지막 아이템에 도달하면 추가 데이터를 로드
                }
            }
        })

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭 이벤트 처리 */
        binding.searchButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {    // 클릭한 시간 차를 계산
                requestSearchScope.launch {
                    val searchTerm = binding.autoCompleteTextView.text.toString()
                    Utils.requestSearch(binding.root.context, searchTerm, model)
                    findNavController().popBackStack()  // 현재 SearchFragment 스택 제거 (mainFragment로 되돌아감)
                    findNavController().navigate(R.id.action_mainFragment_to_searchFragment)                        // mainFragment -> searchFragment로 이동
                    mLastClickTime = SystemClock.elapsedRealtime()  // elapsedRealtime() - 안드로이드 시스템 시간을 나타내는 함수, 시스템 부팅 이후로 경과한 시간(밀리초)을 반환
                }
            }
        }

        /* Enter 키 입력 이벤트 처리 */
        binding.autoCompleteTextView.inputType = EditorInfo.TYPE_CLASS_TEXT
        binding.autoCompleteTextView.maxLines=1  // multiple line 제거
        binding.autoCompleteTextView.imeOptions = EditorInfo.IME_ACTION_SEARCH // Enter키 대신 돋보기가 나타남.

        binding.autoCompleteTextView.setOnEditorActionListener { _, actionId, event ->
            // 소프트 키보드의 "검색" 버튼이나 하드웨어 키보드의 Enter 키를 눌렀을 때
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (SystemClock.elapsedRealtime() - mLastEnterTime > 5000) {  // Enter 키 입력한 시간 차를 계산
                    requestSearchScope.launch {
                        val searchTerm = binding.autoCompleteTextView.text.toString()
                        Utils.requestSearch(binding.root.context, searchTerm, model) // 검색
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                        mLastEnterTime = SystemClock.elapsedRealtime()
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
        searchTermAuto()                    // 검색어 자동 완성
        setVisibility()           // 검색 결과에 따라 레이아웃 가시성 설정
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto(){
        val adapter = Utils.searchTermAuto(binding.root.context, mainDao)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /** 검색 결과에 따라 레이아웃 가시성 설정 */
    private fun setVisibility() {
        if (model.resultList.value.isNullOrEmpty()) {   // 검색 결과가 없을 경우
            binding.saerchResult.visibility = View.VISIBLE
            binding.bottomLinearLayout.visibility = View.GONE
        } else {    // 검색 결과가 있을 경우
            binding.saerchResult.visibility = View.GONE
            binding.bottomLinearLayout.visibility = View.VISIBLE
        }
    }

    /** 검색 결과 Adapter 세팅 */
    private fun setSearchAdapter(){
        model.clearTypeList("searchList")      // searchList 초기화
        searchAdapter = SearchAdapter(model)
        searchAdapter.setClear()       // RecyclerView List 초기

        loadMoreData()

        binding.searchRecyclerView.itemAnimator = null    //  애니메이션 효과 제거
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        binding.searchRecyclerView.adapter = searchAdapter
    }

    /** 검색 목록 - 추가 데이터 로드 */
    private fun loadMoreData() {
        ++page
        startPosition = (page - 1) * 10
        endPosition = startPosition + 10

        model.moveTypeList("searchList",startPosition,endPosition)     // API 검색 결과(ResultList) ->  검색 목록 결과(SearchList) 10개씩 옮기기
    }


}