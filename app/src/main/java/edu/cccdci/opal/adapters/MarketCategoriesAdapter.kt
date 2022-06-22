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

class MarketCategoriesAdapter(
    private val activity: MainActivity,
    private val categoriesList: Array<String>,
    private val hasCurrentAddress: Boolean
) : RecyclerView.Adapter<MarketCategoriesAdapter.MarketCategoriesViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class MarketCategoriesViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from market category card layout
        private val categoryCard: CardView = itemView
            .findViewById(R.id.cv_market_category)
        private val categoryTitle: TextView = itemView
            .findViewById(R.id.tv_market_category_title)
        private val categoryImage: ImageView = itemView
            .findViewById(R.id.iv_market_category_image)

        // Function to set the Market Category Panel attributes
        @SuppressLint("ResourceType")
        internal fun setCategoryPanel(title: String, position: Int) {
            // Change the card background color, depending on the position
            categoryCard.setCardBackgroundColor(
                Color.parseColor(
                    activity.getString(
                        // Odd categories, green. Even categories, teal.
                        if (position % 2 == 0) R.color.primaryColorTheme
                        else R.color.secondaryColorTheme
                    )
                )
            )

            // Change the category image, depending on the position
            categoryImage.setImageResource(
                when (position) {
                    Constants.CATEGORY_MEAT -> R.drawable.ic_category_meat
                    Constants.CATEGORY_SEAFOOD -> R.drawable.ic_category_seafood
                    Constants.CATEGORY_POULTRY -> R.drawable.ic_category_poultry
                    Constants.CATEGORY_FRUITS -> R.drawable.ic_category_fruits
                    Constants.CATEGORY_VEGETABLES -> R.drawable.ic_category_vegetables
                    Constants.CATEGORY_RICE -> R.drawable.ic_category_rice
                    Constants.CATEGORY_OTHERS -> R.drawable.ic_category_others
                    else -> R.drawable.ic_unknown
                }
            )

            // Change the category title
            categoryTitle.text = title

            // Actions when one of the market category cards is clicked
            categoryCard.setOnClickListener {
                if (hasCurrentAddress) {
                    // Make sure the user has selected a current location to proceed
                    Intent(activity, MarketNavActivity::class.java).apply {
                        // Store the market category title
                        putExtra(Constants.CATEGORY_TITLE, title)
                        // Store the market category code
                        putExtra(Constants.CATEGORY_CODE, position)

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

        }  // end of setCategoryPanel method

    }  // end of MarketCategoriesViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MarketCategoriesViewHolder {
        return MarketCategoriesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.market_category_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: MarketCategoriesViewHolder, position: Int) {
        // Get an object from the current position of categories list
        val categoryItem = categoriesList[position]

        // Sets the values of category panel data to the current view
        holder.setCategoryPanel(categoryItem, position)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = categoriesList.size

}  // end of MarketCategoriesAdapter class
