package com.example.stockwatchapp


data class Stock(
    val symbol: String,
    val price: String,
    val changePercent: String,
    val isFavorite: Boolean = false
)