package com.example.appstore.Retrofit2

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

import java.io.Serializable

data class SearchAppDataDto (
    @field:Json(name = "resultCount")
    val resultCount: Int?,

    @field:Json(name = "result")
    val results: List<ApiResult>?
)

@Parcelize
 data class ApiResult(
    /* 아이콘 */
    @field:Json(name = "artworkUrl512")
    val artworkUrl512: String?,

    /* 앱 제목 */
    @field:Json(name = "trackName")
    val trackName: String?,

    /* 평점 */
    @field:Json(name = "averageUserRating")
    val averageUserRating: Float?,

    /* 스크린샷 */
    @field:Json(name = "screenshotUrls")
    val screenshotUrls: List<String>,

    /* 앱 설명 */
    @field:Json(name = "description")
    val description: String?,

    /* 연령 제한 */
    @field:Json(name = "trackContentRating")
    val trackContentRating: String?,

    /* 개발자 */
    @field:Json(name = "artistName")
    val artistName: String?,

    /* 평가 갯수 */
    @field:Json(name = "userRatingCount")
    val userRatingCount: String?,

    /* 분야 */
    @field:Json(name = "primaryGenreName")
    val primaryGenreName: String?,

    /* 버전 정보 */
    @field:Json(name = "releaseNotes")
    val releaseNotes: String?

) : Parcelable

