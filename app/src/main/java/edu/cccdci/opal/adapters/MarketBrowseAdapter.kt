package edu.cccdci.opal.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.MainActivity
import edu.cccdci.opal.ui.activities.MarketNavActivity
import edu.cccdci.opal.utils.Constants

class MarketBrowseAdapter(
    private val activity: MainActivity,
    private val browseList: Array<String>,
    private val browseDescList: Array<String>,
    private val hasCurrentAddress: Boolean
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
        @SuppressLint("ResourceType")
        internal fun setBrowsePanel(title: String, description: String, position: Int) {
            // Change the card background color, depending on the position
            browseCard.setCardBackgroundColor(
                Color.parseColor(
                    activity.getString(
                        when (position) {
                            // Markets Near Me & Recent Markets
                            0, 3 -> R.color.primaryColorTheme  // Green
                            // Bagsak Presyo & Suki Markets
                            else -> R.color.secondaryColorTheme  // Teal
                        }
                    )
                )
            )

            // Change the browse image, depending on the position
            browseImage.setImageResource(
                when (position) {
                    0 -> R.drawable.ic_market_near_me
                    1 -> R.drawable.ic_market_lowest_price
                    2 -> R.drawable.ic_market_suki
                    3 -> R.drawable.ic_market_recent
                    else -> R.drawable.ic_unknown
                }
            )

            // Change the browse title
            browseTitle.text = title

            // Actions when one of the market browse cards is clicked
            browseCard.setOnClickListener {
                // Make sure the user has selected a current location to proceed
                if (hasCurrentAddress) {
                    // Create an Intent to launch MarketNavActivity
                    Intent(activity, MarketNavActivity::class.java).apply {
                        // Store the market browse title & description
                        putExtra(Constants.CATEGORY_TITLE, title)
                        putExtra(Constants.CATEGORY_DESC, description)

                        // Opens the Market Navigation Activity
                        activity.startActivity(this)
                    }  // end of apply
                } else {
                    // Display an error message
                    activity.showSnackBar(
                        activity,
                        activity.getString(R.string.err_no_current_location),
                        true
                    )
                }  // end of if-else
            }  // end of setOnClickListener

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
        // Get an object from the current position of browse description list
        val browseDescItem = browseDescList[position]

        // Sets the values of browse panel data to the current view
        holder.setBrowsePanel(browseItem, browseDescItem, position)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = browseList.size

}  // end of MarketBrowseAdapter class
