package com.example.stockwatchapp

data class ChartResponse(
    val chart: Chart
)

data class Chart(
    val result: List<ChartResult>
)

data class ChartResult(
    val timestamp: List<Long>,
    val indicators: Indicators
)

data class Indicators(
    val quote: List<Quote>
)

data class Quote(
    val close: List<Double?>
)
