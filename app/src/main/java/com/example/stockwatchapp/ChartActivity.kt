package com.example.stockwatchapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stockwatchapp.databinding.ActivityChartBinding
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class ChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding
    private val apiKey = "a8f082be84msh919306b183bdd3ap1f909ejsnae45b71ef880"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val candleStickChart = binding.lineChart as CandleStickChart

        // Get the stock symbol from the intent, or use "AAPL" as default
        val stockSymbol = intent.getStringExtra("STOCK_SYMBOL") ?: "AAPL"

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_favorite -> {
                    val intent = Intent(this, FavoriteActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_chart -> true
                R.id.nav_news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fetchChartData(candleStickChart, stockSymbol)
    }

    private fun fetchChartData(candleStickChart: CandleStickChart, symbol: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yahoo-finance166.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ChartApi::class.java)
        api.getChartData(symbol, "US", "1d", "5m", apiKey).enqueue(object : Callback<ChartResponse> {
            override fun onResponse(call: Call<ChartResponse>, response: Response<ChartResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { chartResponse ->
                        val timestamps = chartResponse.chart.result[0].timestamp
                        val openPrices = chartResponse.chart.result[0].indicators.quote[0].open
                        val closePrices = chartResponse.chart.result[0].indicators.quote[0].close
                        val highPrices = chartResponse.chart.result[0].indicators.quote[0].high
                        val lowPrices = chartResponse.chart.result[0].indicators.quote[0].low
                        displayChart(candleStickChart, timestamps, openPrices, closePrices, highPrices, lowPrices, symbol)
                    }
                } else {
                    Log.e("ChartActivity", "Failed to fetch chart data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ChartResponse>, t: Throwable) {
                Log.e("ChartActivity", "Failed to fetch chart data", t)
            }
        })
    }

    private fun displayChart(
        candleStickChart: CandleStickChart,
        timestamps: List<Long>,
        openPrices: List<Double?>,
        closePrices: List<Double?>,
        highPrices: List<Double?>,
        lowPrices: List<Double?>,
        symbol: String
    ) {
        val entries = mutableListOf<CandleEntry>()

        for (i in timestamps.indices) {
            if (openPrices[i] != null && closePrices[i] != null && highPrices[i] != null && lowPrices[i] != null) {
                val timeInMillis = timestamps[i] * 1000 // Convert to milliseconds
                entries.add(
                    CandleEntry(
                        timeInMillis.toFloat(),
                        highPrices[i]!!.toFloat(),
                        lowPrices[i]!!.toFloat(),
                        openPrices[i]!!.toFloat(),
                        closePrices[i]!!.toFloat()
                    )
                )
            }
        }


        val candleDataSet = CandleDataSet(entries, "$symbol Stock Data")
        candleDataSet.decreasingColor = getColor(R.color.red)
        candleDataSet.decreasingPaintStyle = Paint.Style.FILL
        candleDataSet.increasingColor = getColor(R.color.green)
        candleDataSet.increasingPaintStyle = Paint.Style.FILL
        candleDataSet.neutralColor = getColor(R.color.gray)
        candleDataSet.shadowColorSameAsCandle = true
        candleDataSet.shadowWidth = 1.5f
        candleDataSet.barSpace = 0.1f


        val candleData = CandleData(candleDataSet)
        candleStickChart.data = candleData

        val description = Description()
        description.text = "$symbol Stock Data"
        description.textColor = getColor(R.color.white)
        candleStickChart.description = description

        // Format X-Axis to show date and time
        candleStickChart.xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }

        candleStickChart.invalidate() // Refresh the chart
    }
}
