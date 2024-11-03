package com.example.stockwatchapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApi {
    @GET("api/news/list")
    fun getNewsList(
        @Query("region") region: String = "US",
        @Query("snippetCount") snippetCount: Int = 1000,
        @Header("x-rapidapi-host") host: String = "yahoo-finance166.p.rapidapi.com",
        @Header("x-rapidapi-key") apiKey: String
    ): Call<NewsResponse>
}