package edu.cccdci.opal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Market

class MarketAdapter(
    private val context: Context,
    options: FirestoreRecyclerOptions<Market>
) : FirestoreRecyclerAdapter<Market, MarketAdapter.MarketViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class MarketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from market item layout
        private val name: TextView = itemView.findViewById(R.id.market_name)
        private val address: TextView = itemView.findViewById(R.id.market_address)

        // Function to set values to the specified views from market item
        internal fun setMarketData(market: Market) {
            // Store the market object values in the respective views
            name.text = market.name
            address.text = context.getString(
                R.string.market_address, market.detailAdd, market.barangay,
                market.city, market.province
            )
        }  // end of setMarketData method

    }  // end of MarketViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MarketViewHolder {
        return MarketViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.market_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: MarketViewHolder, position: Int, market: Market
    ) {
        // Sets the values of market data to the current view
        holder.setMarketData(market)
    }  // end of onBindViewHolder method

}  // end of MarketAdapter class