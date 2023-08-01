package com.example.appstore.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.databinding.ItemMultiAgeBinding
import com.example.appstore.databinding.ItemMultiArtistBinding
import com.example.appstore.databinding.ItemMultiChartBinding
import com.example.appstore.databinding.ItemMultiLanguageBinding
import com.example.appstore.databinding.ItemMultiRateBinding
import kotlin.math.roundToInt

class SubInformAdapter(private val result : ApiResult) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val maxItemCount = 5    // 최대 데이터 표시 갯수
    companion object {
        const val ITEM_TYPE_1 = 1
        const val ITEM_TYPE_2 = 2
        const val ITEM_TYPE_3 = 3
        const val ITEM_TYPE_4 = 4
        const val ITEM_TYPE_5 = 5
    }

    /* 각 아이템의 뷰 유형을 결정 (onCreateViewHolder() 메서드가 호출되기 전 RecyclerView 내부에서 자동으로 호출되는 메서드)*/
    override fun getItemViewType(position: Int): Int {
        return when (position % 5) {
            0 -> ITEM_TYPE_1
            1 -> ITEM_TYPE_2
            2 -> ITEM_TYPE_3
            3 -> ITEM_TYPE_4
            else -> ITEM_TYPE_5
        }
    }

    /* 뷰 유형에 따라 해당하는 뷰 홀더를 생성 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_1 -> {
                RateViewHolder(
                    ItemMultiRateBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ITEM_TYPE_2 -> {
                AgeViewHolder(
                    ItemMultiAgeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ITEM_TYPE_3 -> {
                ChartViewHolder(
                    ItemMultiChartBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ITEM_TYPE_4 -> {
                ArtistViewHolder(
                    ItemMultiArtistBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false)
                )
            }
            else -> {
                LanguageViewHolder(
                    ItemMultiLanguageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    /* 뷰 홀더를 바인딩 */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_1 -> {
                val rateViewHolder = holder as RateViewHolder
                rateViewHolder.bind(result)
            }
            ITEM_TYPE_2 -> {
                val ageViewHolder = holder as AgeViewHolder
                ageViewHolder.bind(result)
            }
            ITEM_TYPE_3 -> {
                val chartViewHolder = holder as ChartViewHolder
                chartViewHolder.bind(result)
            }
            ITEM_TYPE_4 -> {
                val artistViewHolder = holder as ArtistViewHolder
                artistViewHolder.bind(result)
            }
            ITEM_TYPE_5 -> {
                val languageViewHolder = holder as LanguageViewHolder
                languageViewHolder.bind(result)
            }
        }
    }

    override fun getItemCount(): Int = maxItemCount


    inner class RateViewHolder(private val binding: ItemMultiRateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.rateTextView1.text = result.userRatingCount + "개의 평가"

            val averageRating = result.averageUserRating ?: 0f
            val roundedRating = (averageRating * 10).roundToInt() / 10.0
            binding.rateTextView2.text = roundedRating.toString()

            binding.rateRatingBar.rating = result.averageUserRating ?: 0f
        }
    }

    inner class AgeViewHolder(private val binding: ItemMultiAgeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.ageTextView1.text = "연령"
            binding.ageTextView2.text = result.trackContentRating
            binding.ageTextView3.text = "세"
        }
    }

    inner class ChartViewHolder(private val binding: ItemMultiChartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.chartTextView1.text = "차트"
            binding.chartTextView2.text = "#15"
            binding.chartTextView3.text = result.primaryGenreName
        }
    }

    inner class ArtistViewHolder(private val binding: ItemMultiArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.artistTextView1.text = "개발자"
            binding.artistTextView2.text = result.artistName
        }
    }

    inner class LanguageViewHolder(private val binding: ItemMultiLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: ApiResult) {
            binding.languageTextView1.text = "언어"
            binding.languageTextView2.text = "KO"
            binding.languageTextView3.text = "한국어"
        }
    }
}