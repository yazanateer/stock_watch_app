package com.example.stockwatchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//class StockAdapter(private val stockList: List<Stock>) :
//    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {
//
//    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val stockSymbol: TextView = itemView.findViewById(R.id.stockSymbol)
//        val stockPrice: TextView = itemView.findViewById(R.id.stockPrice)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_stock, parent, false)
//        return StockViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
//        val stock = stockList[position]
//        holder.stockSymbol.text = stock.symbol
//        holder.stockPrice.text = stock.price
//    }
//
//    override fun getItemCount(): Int = stockList.size
//}

class StockAdapter(private val stockList: List<Stock>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockSymbol: TextView = itemView.findViewById(R.id.stockSymbol)
        val stockPrice: TextView = itemView.findViewById(R.id.stockPrice)
        val stockChangePercent: TextView = itemView.findViewById(R.id.stockChangePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
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
        holder.stockChangePercent.setTextColor(
            holder.itemView.context.getColor(color)
        )
    }

    override fun getItemCount(): Int = stockList.size
}
