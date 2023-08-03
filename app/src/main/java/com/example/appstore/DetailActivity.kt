package com.example.appstore

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appstore.Adapter.ScreenShotAdapter
import com.example.appstore.Adapter.SubInformAdapter
import com.example.appstore.Retrofit2.ApiResult
import com.example.appstore.databinding.ActivityDetailBinding
import com.example.appstore.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class DetailActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Serializable로 전달된 ApiResult를 Intent에서 받아옴.
        // val result = intent.getSerializableExtra("result") as ApiResult

        // Parcelable로 전달된 ApiResult를 Intent에서 받아옴.
          val result = intent.parcelable("result") as ApiResult?

        //val result = intent.getParcelableExtra("result") as ApiResult?

        setInit(result)  // 초기 셋팅

        /* 앱 버전 '더보기' 버튼*/
        binding.showMoreButtonFunction.setOnClickListener {
            toggleAppVersion()     // 전체 내용을 펼치거나 숨기는 작업
        }

        /* 앱 설명 '더보기' 버튼*/
        binding.showMoreButton.setOnClickListener {
            toggleAppDescription()     // 전체 내용을 펼치거나 숨기는 작업
        }

    }

    override fun onResume() {
        super.onResume()
        if (isViewReady()) {
            setNewFunctionButton()
            setDescription()
        }
    }

    private fun isViewReady(): Boolean {
        return binding.root.width > 0 && binding.root.height > 0
    }

    /*      onResume - 액티비티가 포그라운드로 나타날 때 호출
            onWindowFocusChanged() - 액티비티의 창이 포커스를 얻거나 잃을 때 호출

            onResume - 뷰의 상태를 확인하기 전에 실행될 수 있음. 이 경우에는 뷰가 아직 준비되지 않았을 수 있으므로 올바른 동작을 보장하지 않을 수 있음. -> 뷰의 준비 상태를 확인하고 호출

            override fun onResume() {
                super.onResume()
                if (isViewReady()) {
                    setNewFunctionButton()
                    setDescription()
                }
            }

            private fun isViewReady(): Boolean {
                return binding.root.width > 0 && binding.root.height > 0
            }

            이 메서드는 액티비티의 윈도우(Window)가 포커스를 받거나 잃었을 때 호출
            윈도우 포커스 - 사용자가 앱의 윈도우(화면)를 터치하거나 앱이 포그라운드로 나타날 때 윈도우가 포커스를 받는 것
            ex) 다른 앱으로 전환했다가 다시 돌아올 때 앱의 윈도우는 포커스를 받음

            onWindowFocusChanged() 메서드는 다음과 같은 시나리오에서 유용하게 사용될 수 있음
            1) 앱이 활성화되거나 비활성화될 때 특정 동작을 수행해야 할 때
            2) 앱이 포그라운드로 나타났을 때 특정한 UI 업데이트를 해야 할 때
            3) 앱이 포커스를 잃었을 때 작업을 일시 중지하거나 저장해야 할 때     */

    override fun onWindowFocusChanged(hasFocus: Boolean) {      // hasFocus : true - 윈도우가 포커스를 받은 상태, false - 윈도우가 포커스를 잃은 상태
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setNewFunctionButton()
            setDescription()
        }
    }


    /** 초기 세팅 */
    private fun setInit(result : ApiResult?) {
        if (result != null ){        // 검색 결과가 있을 경우
            bindData(result)      // 검색 결과 바인딩
        }
    }

    /** 검색 결과 바인딩 */
    private fun bindData(result: ApiResult) {
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
        var searchList = result.screenshotUrls

        binding.screenshotRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.screenshotRecyclerView.adapter = ScreenShotAdapter(searchList,result,false)

        /*평가,차트,개발자,언어*/
        binding.subInformationRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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