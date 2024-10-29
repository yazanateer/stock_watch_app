package com.example.stockwatchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockwatchapp.LoginActivity
import com.example.stockwatchapp.Stock
import com.example.stockwatchapp.StockAdapter
import com.example.stockwatchapp.StockApi
import com.example.stockwatchapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private lateinit var auth: FirebaseAuth

    private val apiKey = "283c705eb0mshd89645784f48dddp11f3d4jsnde4b5d990cda"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        stockAdapter = StockAdapter(emptyList())
        binding.stockRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.stockRecyclerView.adapter = stockAdapter

        fetchMultipleStocks(listOf("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "NFLX", "FB", "BRK.A", "JPM", "V"))

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchMultipleStocks(symbols: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://yahoo-finance166.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val stockApi = retrofit.create(StockApi::class.java)

            val stockList = mutableListOf<Stock>()

            val requests = symbols.map { symbol ->
                async {
                    try {
                        val response = stockApi.getStockPrice(
                            region = "US",
                            symbol = symbol,
                            apiKey = apiKey
                        ).execute()

                        if (response.isSuccessful) {
                            val stockData = response.body()?.quoteSummary?.result?.firstOrNull()?.price
                            if (stockData != null) {
                                Stock(
                                    symbol = stockData.symbol ?: "",
                                    price = "$${stockData.regularMarketPrice?.raw ?: 0.0}",
                                    changePercent = stockData.regularMarketChangePercent?.fmt ?: "0%"
                                )
                            } else null
                        } else {
                            Log.e("MainActivity", "Error fetching $symbol: ${response.message()}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Exception fetching $symbol", e)
                        null
                    }
                }
            }

            val results = requests.awaitAll().filterNotNull()

            withContext(Dispatchers.Main) {
                stockAdapter = StockAdapter(results)
                binding.stockRecyclerView.adapter = stockAdapter
            }
        }
    }




}
