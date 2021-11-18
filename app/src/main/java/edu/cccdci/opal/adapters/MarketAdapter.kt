package edu.cccdci.opal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Market

class MarketAdapter(
    private val marketDataList: MutableList<Market>
) : RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    // Nested Class to hold views from the target layout
    class MarketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.market_name)
        val address: TextView = itemView.findViewById(R.id.market_address)
    }  // end of MarketViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        return MarketViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.market_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        // Get the object from the current position of the data list
        val marketData = marketDataList[position]

        // Store the marketData values in the respective views
        holder.name.text = marketData.name
        holder.address.text = marketData.address
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = marketDataList.size

}  // end of MarketAdapter class