package com.example.stockwatchapp

data class StockPriceResponse(
    val quoteSummary: QuoteSummary
)

data class QuoteSummary(
    val result: List<PriceResult>?
)

data class PriceResult(
    val price: StockPrice
)

data class StockPrice(
    val symbol: String?,
    val regularMarketPrice: MarketPrice?,
    val regularMarketChangePercent: MarketChangePercent?

)

data class MarketPrice(
    val raw: Double
)

data class MarketChangePercent(
    val raw: Double,
    val fmt: String
)