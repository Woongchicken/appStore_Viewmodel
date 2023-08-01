package com.example.appstore.Retrofit2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiObject {
    private const val BASE_URL = "https://itunes.apple.com/"

    //  Gson은 Google에서 제공하는 JSON 처리 라이브러리로, 가장 널리 사용되는 라이브러리 중 하나입니다. Gson은 Java 객체와 JSON 사이의 변환을 간편하게 처리할 수 있도록 설계되었습니다. Gson은 주로 애노테이션을 사용하여 객체와 JSON 필드 간의 매핑을 정의하며, 객체를 직렬화하거나 역직렬화할 때 유연성을 제공합니다. Gson은 사용하기 쉽고 널리 알려져 있으며, 커스터마이징 가능한 기능도 많이 제공합니다.
    //  Moshi는 Square에서 개발한 JSON 처리 라이브러리로, Gson보다 최신이며 최적화되어 있습니다. Moshi는 Kotlin에 특히 적합한 라이브러리이며, Kotlin의 데이터 클래스와 함께 잘 작동합니다. Moshi는 Gson과 유사한 애노테이션 기반의 매핑을 제공하지만, 더 간결하고 성능이 우수합니다. 또한 Moshi는 Kotlin의 null 안전성을 지원하며, JSON과 Kotlin 타입 간의 자동 변환도 처리할 수 있습니다.

    private val getRetrofit by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val getRetrofitService : AppStoreAPI by lazy { getRetrofit.create(AppStoreAPI::class.java)}
}