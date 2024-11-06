package com.example.stockwatchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(
    private var stockList: List<Stock>,
    private val onFavoriteClick: (String) -> Unit, // Callback for favorite icon click
    private val onItemClick: (String) -> Unit, // Callback for item click
    private val itemLayout: Int // Layout resource for the item view
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockSymbol: TextView = itemView.findViewById(R.id.stockSymbol)
        val stockPrice: TextView = itemView.findViewById(R.id.stockPrice)
        val stockChangePercent: TextView = itemView.findViewById(R.id.stockChangePercent)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.stockSymbol.text = stock.symbol
        holder.stockPrice.text = stock.price
        holder.stockChangePercent.text = stock.changePercent

        // Set text color based on positive or negative change
        val color = if (stock.changePercent.startsWith("-")) {
            android.R.color.holo_red_dark
        } else {
            android.R.color.holo_green_dark
        }
        holder.stockChangePercent.setTextColor(holder.itemView.context.getColor(color))

        // Set the favorite icon based on whether the stock is a favorite
        if (stock.isFavorite) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_favorite) // Filled heart icon
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_unfavorite) // Empty heart icon
        }

        // Handle favorite icon click
        holder.favoriteIcon.setOnClickListener {
            onFavoriteClick(stock.symbol)
        }

        // Handle item click to open ChartActivity
        holder.itemView.setOnClickListener {
            onItemClick(stock.symbol)
        }
    }

    override fun getItemCount(): Int = stockList.size

    // Method to return the current stock list
    fun getStockList(): List<Stock> {
        return stockList
    }

    // Function to update the stock list and refresh the adapter
    fun updateData(newStockList: List<Stock>) {
        stockList = newStockList
        notifyDataSetChanged()
    }
}

