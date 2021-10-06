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
): RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    class MarketViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {
        val name: TextView = itemView.findViewById(R.id.market_name)
        val address: TextView = itemView.findViewById(R.id.market_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder =
        MarketViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.market_card, parent, false
            )
        )

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        val marketData = marketDataList[position]

        holder.name.text = marketData.name
        holder.address.text = marketData.address
    }

    override fun getItemCount(): Int = marketDataList.size

}