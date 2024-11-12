package com.example.stockwatchapp.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stockwatchapp.model.NewsItem
import com.example.stockwatchapp.R

class NewsAdapter(private var newsList: List<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun updateData(newData: List<NewsItem>) {
        newsList = newData
        notifyDataSetChanged()
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.newsTitle)
        val dateTextView: TextView = itemView.findViewById(R.id.newsPubDate)
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.newsThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        val content = newsItem.content

        // Set title and date
        holder.titleTextView.text = content.title
        holder.dateTextView.text = content.pubDate

        // Load thumbnail image using Glide
        val thumbnailUrl = content.thumbnail.resolutions.firstOrNull()?.url
        if (thumbnailUrl != null) {
            Glide.with(holder.itemView.context)
                .load(thumbnailUrl)
                .into(holder.thumbnailImageView)
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_error)
        }

        // Set click listener to open the news URL in a browser
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content.clickThroughUrl.url))
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = newsList.size
}
