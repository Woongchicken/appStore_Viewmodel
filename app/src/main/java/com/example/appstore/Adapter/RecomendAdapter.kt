package com.example.appstore.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appstore.DetailActivity
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.SearchActivity
import com.example.appstore.Utils
import com.example.appstore.databinding.ItemHistoryBinding
import com.example.appstore.databinding.ItemRecomendBinding
import java.io.Serializable

class RecomendAdapter :
    RecyclerView.Adapter<RecomendAdapter.RecomendViewHolder>() {
    private val resultList: MutableList<ApiResult> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomendViewHolder {
        return RecomendViewHolder(
            ItemRecomendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecomendViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    override fun getItemCount(): Int = resultList.size

    inner class RecomendViewHolder(private val binding: ItemRecomendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: ApiResult) {
            binding.ratingBar.rating = result.averageUserRating ?: 0f                      // 평점
            binding.title.text = result.trackName                                          // 타이틀

            Glide.with(binding.root.context)
                .load(result.artworkUrl512)
                .into(binding.img)

            binding.comRowid.setOnClickListener {
                Utils.startDetailActivity(binding.root.context, result)  // 상세 페이지로 이동
            }
        }

    }

    fun setList(apiResultList: List<ApiResult>) {
        Log.d("무한 스크롤", "NoticeAdapter - setList() - apiResultList : $apiResultList")
        resultList.addAll(apiResultList)
    }
    fun deleteLoading(){
        Log.d("무한 스크롤", "NoticeAdapter - deleteLoading()")
        resultList.removeAt(resultList.lastIndex)
    }

}