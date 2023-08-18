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
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private lateinit var binding : FragmentMainBinding

    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수
    private var mLastEnterTime : Long = 0    // Enter 키 입력 시간 저장 변수

    private lateinit var recomendAdapter : RecomendAdapter

    lateinit var model: MainViewModel

    private val mainDao by lazy {
        model.mainDao
    }

    private val resultList by lazy {
        model.resultList.value
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        // 뷰 모델 프로바이더를 통해 뷰모델 가져오기
        model = ViewModelProvider(requireActivity())[MainViewModel::class.java] // requireActivity() - 현재 Fragment가 속한 Activity의 참조를 반환

        /** 화마지막 검색 목록 Adapter 세팅 */
        binding.recomendRecyclerView.apply {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("마지막 검색 목록","MainActivity - setRecentAdapter(1) [${Thread.currentThread().name}]")
                Utils.setRecomend(mainDao, model)
                Log.d("마지막 검색 목록","MainActivity - setRecentAdapter(2) [${Thread.currentThread().name}]")

                if (!(resultList.isNullOrEmpty())) {
                    binding.recomendRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                    // recomendAdapter = RecomendAdapter(model)
                    binding.recomendRecyclerView.adapter = recomendAdapter
                }
            }
        }

        // 뷰모델이 가지고 있는 값의 변경사항을 관찰할 수 있는 라이브 데이터를 옵저빙한다
        model.resultList.observe(viewLifecycleOwner, Observer {// viewLifecycleOwner - 뷰의 생명주기와 연관되어 있어서, Fragment의 뷰가 생성되고 파괴될 때 자동으로 관찰을 시작하고 중단
            Log.d("무한 스크롤", "MainActivity - model.resultList.observe(1) : $it")

            // 초기화되지 않은 경우에만 어댑터 초기화
            if (!::recomendAdapter.isInitialized) {
                recomendAdapter = RecomendAdapter(model)  // RecomendAdapter 초기화
                Log.d("무한 스크롤", "MainActivity - model.resultList.observe(2)")
            }

            recomendAdapter.setList(it)
            recomendAdapter.notifyItemRangeInserted(0, 10)
        })


        binding.recomendRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d("무한 스크롤", "MainActivity - addOnScrollListener - onScrolled")
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount-1

                if (!binding.recomendRecyclerView.canScrollVertically(1) && lastVisibleItemPosition == itemTotalCount) {
                    recomendAdapter.deleteLoading()
                }
            }
        })

        setInit()   // 초기 셋팅

        /* 검색창 클릭할때마다 검색어 자동 완성 매서드 호출 */
        binding.autoCompleteTextView.setOnClickListener {
            searchTermAuto()
        }

        /* 검색 버튼 클릭 이벤트 처리 */
        binding.searchButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {    // 클릭한 시간 차를 계산
                val searchTerm = binding.autoCompleteTextView.text.toString()
                Utils.requestSearch(binding.root.context, searchTerm, mainDao, model) // 검색
                it.findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
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
                    Utils.requestSearch(binding.root.context, searchTerm, mainDao, model) // 검색
                    findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                    mLastEnterTime = SystemClock.elapsedRealtime()
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setInit()   // 초기 셋팅
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
        binding.newLookRecyclerView.adapter = HistoryAdapter(historyEntityList, mainDao, model)
    }



}