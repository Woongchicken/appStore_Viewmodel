package com.example.appstore.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appstore.Adapter.ScreenShotAdapter
import com.example.appstore.Adapter.SubInformAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.ViewModel.MainViewModel
import com.example.appstore.databinding.FragmentDetailBinding
import kotlin.math.roundToInt

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    lateinit var model: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        model = ViewModelProvider(requireActivity())[MainViewModel::class.java] // requireActivity() - 현재 Fragment가 속한 Activity의 참조를 반환

        val result = model.result.value

        setInit(binding,result)  // 초기 셋팅

        /* 앱 버전 '더보기' 버튼*/
        binding.showMoreButtonFunction.setOnClickListener {
            toggleAppVersion()     // 전체 내용을 펼치거나 숨기는 작업
        }

        /* 앱 설명 '더보기' 버튼*/
        binding.showMoreButton.setOnClickListener {
            toggleAppDescription()     // 전체 내용을 펼치거나 숨기는 작업
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isViewReady()) {
            setNewFunctionButton()
            setDescription()
        }
    }

    private fun isViewReady(): Boolean {
        return view?.width ?: 0 > 0 && view?.height ?: 0 > 0
    }

    /** 초기 세팅 */
    private fun setInit(binding: FragmentDetailBinding, result : ApiResult?) {
        if (result != null ){        // 검색 결과가 있을 경우
            bindData(binding, result)      // 검색 결과 바인딩
        }
    }

    /** 검색 결과 바인딩 */
    private fun bindData(binding: FragmentDetailBinding, result: ApiResult) {
        /*제목*/
        binding.trackName.text = result.trackName

        /*제작자*/
        binding.artistName.text = result.artistName
        binding.artist.text = result.artistName

        /*아이콘*/
        Glide.with(this)
            .load(result.artworkUrl512)
            .into(binding.img)

        /*버전 정보*/
        binding.newFunction.text = result.releaseNotes

        /*앱 설명*/
        binding.description.text = result.description

        /*평점 갯수*/
        binding.rateLinearLayoutUserRatingCount.text = result.userRatingCount + "개의 평가"

        /*평점 별*/
        val averageRating = result.averageUserRating ?: 0f
        val roundedRating = (averageRating * 10).roundToInt() / 10.0
        binding.rateLinearLayoutAverageUserRating.text = roundedRating.toString()

        /*평점 별*/
        binding.ratingBar.rating = result.averageUserRating ?: 0f

        /*스크린샷*/
        var screenShotes = result.screenshotUrls
        binding.screenshotRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.screenshotRecyclerView.adapter = ScreenShotAdapter(model, screenShotes, result, false)

        /*평가,차트,개발자,언어*/
        binding.subInformationRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.subInformationRecyclerView.adapter = SubInformAdapter(result)
    }

    /* 앱 버전 - '더보기' 버튼 표시 */
    private fun setNewFunctionButton(){
        val lines = binding.newFunction.lineCount       // TextView에 텍스트가 렌더링된 후에만 정확한 값을 가지기 때문에 onCreate() 메서드에서 호출하면 정확한 줄 수를 얻을 수 없음..
        if (lines > 1){
            binding.showMoreButtonFunction.visibility = View.VISIBLE
        }else {
            binding.showMoreButtonFunction.visibility = View.GONE
        }
    }


    /* 앱 버전 - 설명 표시 */
    private fun toggleAppVersion() {
        val newFunction = binding.newFunction
        if (newFunction.maxLines == 1) {        // maxLines - 텍스트를 주어진 줄 수로 제한
            // 펼쳐진 상태에서 숨김
            newFunction.maxLines = Int.MAX_VALUE        // MAX_VALUE - 텍스트의 최대 줄 수를 나타내는 상수
            binding.showMoreButtonFunction.text = "접기"
        } else {
            // 숨겨진 상태에서 펼침
            newFunction.maxLines = 1
            binding.showMoreButtonFunction.text = "더보기"
        }
    }

    /*앱 설명 - '더보기' 버튼 표시 */
    private fun setDescription(){
        val lines = binding.description.lineCount
        if (lines > 3){
            binding.showMoreButton.visibility = View.VISIBLE
        }else {
            binding.showMoreButton.visibility = View.GONE
        }
    }

    /*앱 설명 - 설명 표시 */
    private fun toggleAppDescription() {
        val description = binding.description
        if (description.maxLines == 3) {        // maxLines - 텍스트를 주어진 줄 수로 제한
            // 펼쳐진 상태에서 숨김
            description.maxLines = Int.MAX_VALUE        // MAX_VALUE - 텍스트의 최대 줄 수를 나타내는 상수
            binding.showMoreButton.text = "접기"
        } else {
            // 숨겨진 상태에서 펼침
            description.maxLines = 3
            binding.showMoreButton.text = "더보기"
        }
    }
}