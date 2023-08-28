package com.example.appstore.Retrofit2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AppStoreAPI {
    @GET("/search")
    fun getApp(@Query("term") term: String, @Query("media") media: String = "software", @Query("limit") limit: Int = 200): Call<SearchAppDataDto>
}