package com.example.stockwatchapp

data class StockResponse(
    val quoteResponse: QuoteResponse
)

data class QuoteResponse(
    val result: List<StockResult>
)

data class StockResult(
    val symbol: String,
    val regularMarketPrice: Double,
    val regularMarketChangePercent: String
)
