package edu.cccdci.opal.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.MarketNavActivity
import edu.cccdci.opal.utils.Constants

class MarketBrowseAdapter(
    private val context: Context,
    private val curLoc: Array<Double>,
    private val browseList: Array<String>
) : RecyclerView.Adapter<MarketBrowseAdapter.MarketBrowseViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class MarketBrowseViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from market browse card layout
        private val browseCard: CardView = itemView
            .findViewById(R.id.cv_market_browse)
        private val browseTitle: TextView = itemView
            .findViewById(R.id.tv_market_browse_title)
        private val browseImage: ImageView = itemView
            .findViewById(R.id.iv_market_browse_image)

        // Function to set the Market Browse Panel attributes
        internal fun setBrowsePanel(title: String, position: Int) {
            // Change the card background color, depending on the position
            browseCard.setCardBackgroundColor(
                Color.parseColor(
                    // Odd categories, green. Even categories, teal.
                    if (position % 2 == 0) Constants.APP_GREEN else Constants.APP_TEAL
                )
            )

            // Change the browse image, depending on the position
            browseImage.setImageResource(
                when (position) {
                    // Top Sales
                    0 -> R.drawable.ic_market_top_sales
                    // Bagsak Presyo (Lowest Price)
                    1 -> R.drawable.ic_market_lowest_price
                    // Recent Markets
                    2 -> R.drawable.ic_market_recent
                    // Unknown
                    else -> R.drawable.ic_unknown
                }
            )

            // Change the browse title
            browseTitle.text = title

            browseCard.setOnClickListener {
                val intent = Intent(
                    context, MarketNavActivity::class.java
                )

                intent.putExtra(
                    Constants.CURRENT_LOCATION,
                    LatLng(curLoc[0], curLoc[1])
                )

                intent.putExtra("category_title", title)

                context.startActivity(intent)
            }
        }  // end of setBrowsePanel method

    }  // end of MarketBrowseViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MarketBrowseViewHolder {
        return MarketBrowseViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.market_browse_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: MarketBrowseViewHolder, position: Int
    ) {
        // Get an object from the current position of browse list
        val browseItem = browseList[position]

        // Sets the values of browse panel data to the current view
        holder.setBrowsePanel(browseItem, position)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = browseList.size

}  // end of MarketBrowseAdapter class
