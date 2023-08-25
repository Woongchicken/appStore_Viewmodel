package com.example.appstore.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appstore.R
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentDetailBinding
import com.example.appstore.databinding.ItemSearchBinding

class SearchAdapter(private val model: MainViewModel) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private val resultList: MutableList<ApiResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            ItemSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    override fun getItemCount(): Int = resultList.size


    inner class SearchViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.ratingBar.rating = result.averageUserRating ?: 0f                      // 평점
            binding.title.text = result.trackName                                          // 타이틀

            Glide.with(binding.root.context)                                                // 아이콘
                .load(result.artworkUrl512)
                .into(binding.img)

            /*스크린샷*/
            var screenShotes = result.screenshotUrls
            binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerView.adapter = ScreenShotAdapter(model, screenShotes, result, true)

            binding.comRowid.setOnClickListener {
                model.setResult(result)
                it.findNavController().navigate(R.id.action_searchFragment_to_detailFragment)
            }
        }
    }

    fun setList(apiResultList: List<ApiResult>) {
        resultList.addAll(apiResultList)
    }

    fun setClear() {
        resultList.clear()
    }
}

