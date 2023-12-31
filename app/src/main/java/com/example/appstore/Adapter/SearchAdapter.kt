package com.example.appstore.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appstore.R
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Utils
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentDetailBinding
import com.example.appstore.databinding.ItemSearchBinding
import com.example.appstore.main.SearchFragmentDirections

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

        //Preload
        if (position <= resultList.size) {
            val endPosition = if (position + 10 > resultList.size) {
                resultList.size
            } else {
                position + 10
            }
            resultList.subList(position, endPosition).map{ it.artworkUrl512 }.forEach {
                Utils.preload(holder.itemView.context, it!!)
            }
        }
    }

    override fun getItemCount(): Int = resultList.size


    inner class SearchViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.ratingBar.rating = result.averageUserRating ?: 0f                      // 평점
            binding.title.text = result.trackName                                          // 타이틀

            // 아이콘
            Glide.get(binding.root.context)
                .setMemoryCategory(MemoryCategory.HIGH)     // Glide가 앱의 특정 부분에서 더 많은 메모리를 일시적으로 사용하도록 허용

            Glide.with(binding.root.context)
                .load(result.artworkUrl512)
                .diskCacheStrategy(DiskCacheStrategy.ALL)    // 디스크 캐시전략 - DiskCacheStrategy.ALL: 원래 그림도 캐시하고 변환된 그림도 캐시
                .placeholder(R.drawable.loading)            // 원본 Image를 보여주기 전 잠깐 보여주는 Image를 지정
                .error(R.drawable.error)                   // Image를 로드 중에 에러가 발생할 경우 보여주는 Image를 지정
                .into(binding.img)

            /*스크린샷*/
            var screenShotes = result.screenshotUrls
            binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerView.adapter = ScreenShotAdapter(model, screenShotes, result, true)

            binding.comRowid.setOnClickListener {
                // model.setResult(result)      // ViewModel을 통해 선택한 데이터 전달
                // it.findNavController().navigate(R.id.action_searchFragment_to_detailFragment)

                val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(result)      // Navagation Safe Args
                it.findNavController().navigate(action)
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

