package com.example.stockwatchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockwatchapp.adapters.NewsAdapter
import com.example.stockwatchapp.api.NewsApi
import com.example.stockwatchapp.databinding.ActivityNewsBinding
import com.example.stockwatchapp.model.NewsResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    private val apiKey = "879e4beac3mshca50202beb6d4dep1745d3jsna7991a62b9c9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.selectedItemId = R.id.nav_news

        newsAdapter = NewsAdapter(emptyList())
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.newsRecyclerView.adapter = newsAdapter



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
                R.id.nav_chart -> {
                    val intent = Intent(this, ChartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_news -> true
                else -> false
            }
        }



        fetchNews()
    }

    private fun fetchNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yahoo-finance166.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsApi = retrofit.create(NewsApi::class.java)

        newsApi.getNewsList(apiKey = apiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsList = response.body()?.data?.main?.stream ?: emptyList()
                    newsAdapter.updateData(newsList)
                } else {
                    Log.e("NewsActivity", "Response unsuccessful: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("NewsActivity", "Failed to fetch news", t)
            }
        })
    }
}
