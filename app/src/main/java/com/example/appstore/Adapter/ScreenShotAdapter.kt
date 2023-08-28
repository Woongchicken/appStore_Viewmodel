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
import com.example.appstore.databinding.ItemScreenBinding

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
    }

    override fun getItemCount(): Int = screenShotes.size

    inner class ScreenShotViewHolder(private val binding: ItemScreenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(screenShot: String, result: ApiResult, isClickEventEnabled: Boolean) {
            Glide.with(binding.root.context).load(screenShot).into(binding.screenShot)

            /* 상세페이지 - 스크린샷 클릭 -> 아무 반응 없음.  검색페이지 - 스크린샷 클릭 -> 상세페이지로 이동 */
            if(isClickEventEnabled) {
                binding.comRowid.setOnClickListener {
                    // ViewModel을 통해 선택한 데이터 전달
                    model.setResult(result)
                    Log.d("내비게이션", "ScreenShotAdapter : searchFragment-> detailFragment")
                    it.findNavController().navigate(R.id.action_searchFragment_to_detailFragment)
                }
            }
        }
    }
}