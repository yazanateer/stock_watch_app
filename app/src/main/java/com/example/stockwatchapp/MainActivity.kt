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
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val apiKey = "c216b2c236mshaf28311583808d1p1b1dadjsn91fa5ce0e4be"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        stockAdapter = StockAdapter(
            emptyList(),
            { symbol -> addToFavorites(symbol) },
            { symbol -> openChartActivity(symbol) },
            R.layout.item_stock // Pass the layout with empty heart icon
        )

        binding.stockRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.stockRecyclerView.adapter = stockAdapter

        binding.menuIcon.setOnClickListener {
            showPopupMenu(it)
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_favorite -> {
                    val intent = Intent(this, FavoriteActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_chart -> {
                    val intent = Intent(this, ChartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fetchMultipleStocks(listOf("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "NFLX", "FB", "BRK.A", "JPM", "V"))
    }

    private fun openChartActivity(symbol: String) {
        val intent = Intent(this, ChartActivity::class.java)
        intent.putExtra("STOCK_SYMBOL", symbol) // Pass the stock symbol to ChartActivity
        startActivity(intent)
    }

    private fun addToFavorites(symbol: String) {
        val userId = auth.currentUser?.uid ?: return

        // Add the stock symbol to the user's favoriteStocks list
        val favoriteStockRef = database.child("users").child(userId).child("favoriteStocks").child(symbol)

        favoriteStockRef.setValue(true)
            .addOnSuccessListener {
                Log.d("MainActivity", "Added $symbol to favorites")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to add $symbol to favorites", e)
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
                stockAdapter.updateData(results)
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}