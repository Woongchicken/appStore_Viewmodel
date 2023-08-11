package com.example.appstore.Adapter

import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.DetailActivity
import com.example.appstore.MainViewModel
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Room.HistoryEntity
import com.example.appstore.Room.MainDao
import com.example.appstore.SearchActivity
import com.example.appstore.Utils
import com.example.appstore.databinding.ActivityMainBinding
import com.example.appstore.databinding.ItemHistoryBinding
import java.io.Serializable

class HistoryAdapter(private val historyEntityList: List<HistoryEntity>, private val mainDao: MainDao, private var model : MainViewModel) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val maxItemCount = 6        // 최대 데이터 표시 갯수
    private var mLastClickTime : Long = 0    // Click 키 입력 시간 저장 변수


    /* 뷰 홀더 생성 (호출되는 횟수가 정해져있음) */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),    // context
                parent,                                 // 부모
                false                      // 부모와 연결 / false - 리사이클러뷰가 알아서 해줌
            )
        )
    }

    /* 뷰 홀더 바인드 (스크롤 내릴때마다 호출) */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyEntityList[position])        // position = holder.bindingAdapterPosition
    }

    override fun getItemCount(): Int = minOf(maxItemCount, historyEntityList.size)

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(history: HistoryEntity) {
            binding.searchTerm.text = history.searchTerm
            binding.comRowid.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime > 2000) { // 클릭한 시간 차를 계산
                    Utils.requestSearch(binding.root.context, history.searchTerm, mainDao, model) // 검색
                }
                mLastClickTime = SystemClock.elapsedRealtime()
            }
        }
    }


}