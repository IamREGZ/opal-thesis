package edu.cccdci.opal.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.ui.activities.MarketPageActivity
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GeoDistance
import edu.cccdci.opal.utils.GlideLoader

class MarketAdapter(
    private val activity: Activity,
    private val marketList: List<Market>,
    private val currentLoc: List<Double>
) : RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class MarketViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), GeoDistance.GeoDistanceResult {
        // Get all the ids of views from market item layout
        private val marketPanel: CardView = itemView
            .findViewById(R.id.cv_market)
        private val name: TextView = itemView
            .findViewById(R.id.tv_market_name)
        private val category: TextView = itemView
            .findViewById(R.id.tv_market_category)
        private val address: TextView = itemView
            .findViewById(R.id.tv_market_address)
        private val distance: TextView = itemView
            .findViewById(R.id.tv_market_delivery_distance)
        private val duration: TextView = itemView
            .findViewById(R.id.tv_market_delivery_duration)
        private val deliveryFee: TextView = itemView
            .findViewById(R.id.tv_market_delivery_fee)
        private val rating: TextView = itemView
            .findViewById(R.id.tv_market_rating)
        private val mktImage: ImageView = itemView
            .findViewById(R.id.market_image)

        // Array to store category list
        private val categoryList: Array<String> = activity.resources
            .getStringArray(R.array.market_categories_list)

        // Function to set values to the specified views from market item
        internal fun setMarketData(market: Market) {
            // Store the market object values in the respective views
            name.text = market.name
            address.text = activity.getString(
                R.string.market_address, market.city, market.province
            )
            deliveryFee.text = activity.getString(
                R.string.item_price, market.deliveryFee
            )
            rating.text = activity.getString(
                R.string.rating_count, market.ratings
            )
            category.text = if (
                categoryList[market.category] == Constants.ITEM_OTHERS
            )
                activity.getString(
                    R.string.market_other_category,
                    categoryList[market.category],
                    market.otherCat
                )
            else
                categoryList[market.category]

            GlideLoader(activity).loadImage(market.image, mktImage)

            GeoDistance(activity, this).calculateDistance(
                Constants.getLocation(market), currentLoc
            )

            marketPanel.setOnClickListener {
                val intent = Intent(
                    activity, MarketPageActivity::class.java
                )

                intent.putExtra(Constants.MARKET_INFO, market)

                activity.startActivity(intent)
            }
        }  // end of setMarketData method

        override fun setDistanceResult(res: String) {
            // Extract the values, separated by commas
            val distRes: List<String> = res.split(',')

            distance.text = activity.getString(
                R.string.delivery_distance_value,
                distRes[0].toDouble() / 1000
            )

            duration.text = activity.getString(
                R.string.delivery_duration_value,
                distRes[1].toInt() / 60
            )
        }  // end of setDistanceResult method

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
    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        // Get an object from the current position of market list
        val marketItem = marketList[position]

        // Sets the values of market data to the current view
        holder.setMarketData(marketItem)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = marketList.size

}  // end of MarketAdapter class
