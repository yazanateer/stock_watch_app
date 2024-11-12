package com.example.stockwatchapp.api

import com.example.stockwatchapp.model.ChartResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ChartApi {
    @GET("api/stock/get-chart")
    fun getChartData(
        @Query("symbol") symbol: String,
        @Query("region") region: String,
        @Query("range") range: String,
        @Query("interval") interval: String,
        @Header("x-rapidapi-key") apiKey: String
    ): Call<ChartResponse>
}