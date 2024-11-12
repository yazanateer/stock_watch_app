package com.example.stockwatchapp.api

import com.example.stockwatchapp.model.StockPriceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StockApi {
    @GET("api/stock/get-price")
    fun getStockPrice(
        @Query("region") region: String,
        @Query("symbol") symbol: String,
        @Header("x-rapidapi-key") apiKey: String,
        @Header("x-rapidapi-host") host: String = "yahoo-finance166.p.rapidapi.com"
    ): Call<StockPriceResponse>
}
