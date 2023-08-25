package com.example.appstore.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appstore.R
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ItemProgressBinding
import com.example.appstore.databinding.ItemRecomendBinding
import java.text.FieldPosition


class RecomendAdapter(private val model: MainViewModel) :
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
                model.setResult(result)
                it.findNavController().navigate(R.id.action_mainFragment_to_detailFragment)
            }
        }
    }

    fun setList(apiResultList: List<ApiResult>) {
        resultList.addAll(apiResultList)
    }

    fun setClear(){
        resultList.clear()
    }
}