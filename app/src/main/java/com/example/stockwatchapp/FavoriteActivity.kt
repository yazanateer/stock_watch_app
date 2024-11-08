package com.example.stockwatchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockwatchapp.databinding.ActivityFavoriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var favoriteAdapter: StockAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val apiKey = "a8f082be84msh919306b183bdd3ap1f909ejsnae45b71ef880"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        favoriteAdapter = StockAdapter(
            emptyList(),
            onFavoriteClick = { symbol ->
                removeFromFavorites(symbol)
                // Placeholder for favorite action, e.g., remove from favorites
                Log.d("FavoriteActivity", "Favorite clicked for $symbol")
            },
            onItemClick = { symbol ->
                // Navigate to ChartActivity with the stock symbol
                val intent = Intent(this, ChartActivity::class.java).apply {
                    putExtra("STOCK_SYMBOL", symbol)
                }
                startActivity(intent)
            },
            R.layout.item_stock_favoried // Pass the layout with filled heart icon
        )

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favoriteRecyclerView.adapter = favoriteAdapter

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_favorite -> true
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
        loadFavoriteStocks()
    }

    private fun loadFavoriteStocks() {
        val userId = auth.currentUser?.uid ?: return
        val favoriteStocksRef = database.child("users").child(userId).child("favoriteStocks")

        favoriteStocksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val symbols = snapshot.children.mapNotNull { it.key }
                fetchStockData(symbols)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoriteActivity", "Failed to load favorite stocks", error.toException())
            }
        })
    }

    private fun fetchStockData(symbols: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://yahoo-finance166.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val stockApi = retrofit.create(StockApi::class.java)

            val requests = symbols.map { symbol ->
                async {
                    try {
                        val response = stockApi.getStockPrice("US", symbol, apiKey).execute()
                        response.body()?.quoteSummary?.result?.firstOrNull()?.price?.let {
                            Stock(
                                symbol = it.symbol ?: "",
                                price = "$${it.regularMarketPrice?.raw ?: 0.0}",
                                changePercent = it.regularMarketChangePercent?.fmt ?: "0%",
                                isFavorite = true
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("FavoriteActivity", "Error fetching $symbol: ${e.message}")
                        null
                    }
                }
            }

            val stocks = requests.awaitAll().filterNotNull()

            withContext(Dispatchers.Main) {
                favoriteAdapter.updateData(stocks) // Update adapter with the fetched data
            }
        }
    }

    private fun removeFromFavorites(symbol: String) {
        val userId = auth.currentUser?.uid ?: return
        val favoriteStockRef = database.child("users").child(userId).child("favoriteStocks").child(symbol)

        favoriteStockRef.removeValue()
            .addOnSuccessListener {
                Log.d("FavoriteActivity", "Removed $symbol from favorites")
                // Use getStockList() to get the current stock list and filter it
                val updatedStockList = favoriteAdapter.getStockList().filter { it.symbol != symbol }
                favoriteAdapter.updateData(updatedStockList)
            }
            .addOnFailureListener { e ->
                Log.e("FavoriteActivity", "Failed to remove $symbol from favorites", e)
            }
    }


}

