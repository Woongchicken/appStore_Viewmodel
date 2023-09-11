package com.example.appstore.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appstore.R
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.Utils
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.ItemScreenBinding
import com.example.appstore.main.MainFragmentDirections
import com.example.appstore.main.SearchFragment
import com.example.appstore.main.SearchFragmentDirections

class ScreenShotAdapter(private val model: MainViewModel,  private val screenShotes: List<String>, private val result: ApiResult, private val isClickEventEnabled: Boolean) :
    RecyclerView.Adapter<ScreenShotAdapter.ScreenShotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenShotAdapter.ScreenShotViewHolder {
        return ScreenShotViewHolder(
            ItemScreenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ScreenShotAdapter.ScreenShotViewHolder, position: Int) {
        holder.bind(screenShotes[position], result, isClickEventEnabled)

        //Preload
        if (position <= screenShotes.size) {
            val endPosition = if (position + 4 > screenShotes.size) {
                screenShotes.size
            } else {
                position + 4
            }
            screenShotes.subList(position, endPosition).forEach { imageUrl ->
                Utils.preload(holder.itemView.context, imageUrl)
            }
        }

    }

    override fun getItemCount(): Int = screenShotes.size



    inner class ScreenShotViewHolder(private val binding: ItemScreenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(screenShot: String, result: ApiResult, isClickEventEnabled: Boolean) {
            // 스크린콘
            Glide.get(binding.root.context)
                .setMemoryCategory(MemoryCategory.HIGH)     // Glide가 앱의 특정 부분에서 더 많은 메모리를 일시적으로 사용하도록 허용

            Glide.with(binding.root.context)
                .load(screenShot)
                .override(500, 500) // 이미지 크기 조정
                .diskCacheStrategy(DiskCacheStrategy.ALL)    // 디스크 캐시전략 - DiskCacheStrategy.ALL: 원래 그림도 캐시하고 변환된 그림도 캐시
                .placeholder(R.drawable.loading)            // 원본 Image를 보여주기 전 잠깐 보여주는 Image를 지정
                .error(R.drawable.error)                   // Image를 로드 중에 에러가 발생할 경우 보여주는 Image를 지정
                .into(binding.screenShot)

            /* 상세페이지 - 스크린샷 클릭 -> 아무 반응 없음.  검색페이지 - 스크린샷 클릭 -> 상세페이지로 이동 */
            if(isClickEventEnabled) {
                binding.comRowid.setOnClickListener {
                    // model.setResult(result)                     // ViewModel을 통해 선택한 데이터 전달
                    // it.findNavController().navigate(R.id.action_searchFragment_to_detailFragment)

                    val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(result)      // Navagation Safe Args
                    it.findNavController().navigate(action)
                }
            }
        }
    }

}