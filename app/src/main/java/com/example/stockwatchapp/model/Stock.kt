package com.example.stockwatchapp.model


data class Stock(
    val symbol: String,
    val price: String,
    val changePercent: String,
    val isFavorite: Boolean = false
)