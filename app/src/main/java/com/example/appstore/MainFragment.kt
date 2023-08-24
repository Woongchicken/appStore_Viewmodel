package com.example.appstore

/*

1. 중복 검색 방지 -> 키 입력 시간 저장 변수 설정
2. 코루틴 스코프
3. Activity -> Fragment
4. ViewModel & LiveData

*/

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.Adapter.HistoryAdapter
import com.example.appstore.Adapter.RecomendAdapter
import com.example.appstore.Room.MainDao
import com.example.appstore.Room.RoomDB
import com.example.appstore.Utils.searchTermAuto
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
class MainFragment : Fragment() {
    private lateinit var binding : FragmentMainBinding
    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime : Long = 0    // Enter 키 입력 시간 저장 변수
    private var page = 0       // API 호출 결과 페이지
    private var startPosition = 1
    private var endPosition = 1
    private lateinit var recomendAdapter : RecomendAdapter
    lateinit var model: MainViewModel
    private val mainDao by lazy {
        model.mainDao
    }

    private val recomendScope = CoroutineScope(Dispatchers.Main)        // 마지막 검색 목록 코루틴스코프
    private val requestSearchScope = CoroutineScope(Dispatchers.Main)   // 검색 코루틴스코프

    override fun onResume() {
        super.onResume()
        setInit()   // 초기 셋팅
    }

    override fun onDestroy() {
        super.onDestroy()
        recomendScope.cancel()
        requestSearchScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        page = 0       // API 호출 결과 페이지
        // 뷰 모델 프로바이더를 통해 뷰모델 가져오기
        model = ViewModelProvider(requireActivity())[MainViewModel::class.java] // requireActivity() - 현재 Fragment가 속한 Activity의 참조를 반환
        setInit()

        // recomendList 라이브 데이터를 옵저빙
        model.recomendList.observe(viewLifecycleOwner, Observer {
            if (!::recomendAdapter.isInitialized) {     // !:: 지연 초기화된 프로퍼티의 초기화 상태를 확인, 프로퍼티가 초기화되지 않았을 때 true를 반환
                recomendAdapter = RecomendAdapter(model)
            }
            if(it.size != 0) {
                recomendAdapter.setList(it, startPosition, endPosition)
                recomendAdapter.notifyItemRangeInserted(startPosition, endPosition)
            }
        })

        setRecomendAdapter()    // 마지막 검색 목록 Adapter 세팅

        //  사용자의 스크롤을 감지하는 리스너
        binding.recomendRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                Log.d("무한 스크롤", "MainActivity - addOnScrollListener / lastVisibleItemPosition : ${lastVisibleItemPosition}")
                val itemTotalCount = recyclerView.adapter!!.itemCount-1
                Log.d("무한 스크롤", "MainActivity - addOnScrollListener / itemTotalCount : ${itemTotalCount}")

                // 스크롤 끝에 도달했는지 체크
                if (!binding.recomendRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) { // 1. canScrollVertically - 수직스크롤이 끝에 도달했는지 알려주는 메서드, 최상단에 도달 : -1, 최하단에 도달 : 1 && 2. 데이터의 마지막 아이템이 화면에 뿌려졌는지
                    // 스크롤이 마지막 아이템에 도달하면 추가 데이터를 로드
                    loadMoreData()
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
                    Utils.requestSearch(binding.root.context, searchTerm, mainDao, model) // 검색

                    it.findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                }
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
                    requestSearchScope.launch {
                        val searchTerm = binding.autoCompleteTextView.text.toString()
                        Utils.requestSearch(binding.root.context, searchTerm, mainDao, model) // 검색
                        findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                        mLastEnterTime = SystemClock.elapsedRealtime()
                    }
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }
        return view
    }

    /** 초기 세팅 */
    private fun setInit() {
        searchTermAuto()                    // 검색어 자동 완성
        setHistoryAdapter()                 // 최근 검색어
    }

    /** 검색어 자동 완성 Adapter 세팅 */
    private fun searchTermAuto(){
        val adapter = Utils.searchTermAuto(binding.root.context, mainDao)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /** 최근 검색어  Adapter 세팅 */
    private fun setHistoryAdapter(){
        val historyEntityList = mainDao.getHistoryAll()

        binding.newLookRecyclerView.layoutManager =
            GridLayoutManager(binding.root.context, 2)     //  spanCount - 그리드의 열 수를 나타내는 정수 값
        binding.newLookRecyclerView.adapter = HistoryAdapter(historyEntityList, model, requestSearchScope)
    }

    /** 마지막 검색 목록 Adapter 세팅 */
    private fun setRecomendAdapter(){
        recomendScope.launch {
            model.clearRecomendList()      // recomendList 초기화
            recomendAdapter = RecomendAdapter(model)
            recomendAdapter.setClear()

            Utils.setRecomend(mainDao, model)

            loadMoreData()

            if (!(model.recomendList.value.isNullOrEmpty())) {
                binding.recomendRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                binding.recomendRecyclerView.adapter = recomendAdapter
            }
        }
    }

    /** 마지막 검색 목록 - Data 세팅*/
    private fun loadMoreData() {
        ++page
        startPosition = (page - 1) * 10
        endPosition = startPosition + 10

        model.moveRecomendList(startPosition,endPosition)     // API 검색 결과(ResultList) -> 마지막 검색 목록 결과(RecomendList) 10개씩 옮기기
    }



}