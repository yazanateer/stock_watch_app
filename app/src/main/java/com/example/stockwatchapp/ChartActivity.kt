package com.example.stockwatchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.stockwatchapp.databinding.ActivityChartBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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

        val lineChart = binding.lineChart

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

        fetchChartData(lineChart, stockSymbol)
    }

    private fun fetchChartData(lineChart: LineChart, symbol: String) {
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
                        val closePrices = chartResponse.chart.result[0].indicators.quote[0].close
                        displayChart(lineChart, timestamps, closePrices, symbol)
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

    private fun displayChart(lineChart: LineChart, timestamps: List<Long>, closePrices: List<Double?>, symbol: String) {
        val entries = mutableListOf<Entry>()

        for (i in timestamps.indices) {
            closePrices[i]?.let { closePrice ->
                val timeInMillis = timestamps[i] * 1000 // Convert to milliseconds
                entries.add(Entry(timeInMillis.toFloat(), closePrice.toFloat()))
            }
        }

        val lineDataSet = LineDataSet(entries, "$symbol Stock Price")
        lineDataSet.color = getColor(R.color.primaryColor)
        lineDataSet.valueTextColor = getColor(R.color.secondaryText)
        lineDataSet.lineWidth = 2f

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        val description = Description()
        description.text = "$symbol Stock Price"
        lineChart.description = description

        // Format X-Axis to show date and time
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }

        lineChart.invalidate() // Refresh the chart
    }
}



