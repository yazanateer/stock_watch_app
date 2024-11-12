package com.example.stockwatchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockwatchapp.adapters.StockAdapter
import com.example.stockwatchapp.api.StockApi
import com.example.stockwatchapp.databinding.ActivityMainBinding
import com.example.stockwatchapp.model.Stock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var stockAdapter: StockAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val apiKey = "2fb648d91emshe0554b6bb9ea674p192e08jsnf75517adf13f"
    private val defaultSymbols = listOf("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "NFLX", "FB", "BRK.A", "JPM", "V")
    private var favoriteStocks = mutableSetOf<String>()

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
            R.layout.item_stock
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

        val searchBar: EditText = findViewById(R.id.searchBar)
        val searchButton: ImageView = findViewById(R.id.searchButton)

        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchMultipleStocks(listOf(query))
            } else {
                fetchMultipleStocks(defaultSymbols) // Reset to default list
            }
        }
        fetchFavoriteStocks()
        fetchUserName()
    }

    private fun openChartActivity(symbol: String) {
        val intent = Intent(this, ChartActivity::class.java)
        intent.putExtra("STOCK_SYMBOL", symbol)
        startActivity(intent)
    }

    private fun addToFavorites(symbol: String) {
        val userId = auth.currentUser?.uid ?: return
        val favoriteStockRef = database.child("users").child(userId).child("favoriteStocks").child(symbol)

        favoriteStockRef.setValue(true)
            .addOnSuccessListener {
                Log.d("MainActivity", "Added $symbol to favorites")
                favoriteStocks.add(symbol)
                stockAdapter.notifyDataSetChanged() // Refresh the adapter
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to add $symbol to favorites", e)
            }
    }

    private fun fetchFavoriteStocks() {
        val userId = auth.currentUser?.uid ?: return
        val favoriteStocksRef = database.child("users").child(userId).child("favoriteStocks")

        favoriteStocksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteStocks.clear()
                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.key?.let { favoriteStocks.add(it) }
                }
                fetchMultipleStocks(defaultSymbols) // Fetch main stocks after getting favorites
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to load favorite stocks", error.toException())
            }
        })
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
                                    changePercent = stockData.regularMarketChangePercent?.fmt ?: "0%",
                                    isFavorite = favoriteStocks.contains(stockData.symbol)
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


    private fun fetchUserName() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.child("users").child(userId).child("name")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)
                if (username != null) {
                    binding.welcomeUserText.text = "Welcome, $username"
                } else {
                    binding.welcomeUserText.text = "Welcome, user"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error fetching the username form the database")
                binding.welcomeUserText.text = "Welcome, user"
            }
        })
    }


}
