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

class MarketCategoriesAdapter(
    private val context: Context,
    private val curLoc: Array<Double>,
    private val categoriesList: Array<String>
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
        internal fun setCategoryPanel(title: String, position: Int) {
            // Change the card background color, depending on the position
            categoryCard.setCardBackgroundColor(
                Color.parseColor(
                    // Odd categories, green. Even categories, teal.
                    if (position % 2 == 0) Constants.APP_GREEN else Constants.APP_TEAL
                )
            )

            // Change the category image, depending on the position
            categoryImage.setImageResource(
                when (position) {
                    // Meat
                    0 -> R.drawable.ic_category_meat
                    // Seafood
                    1 -> R.drawable.ic_category_seafood
                    // Poultry
                    2 -> R.drawable.ic_category_poultry
                    // Fruits
                    3 -> R.drawable.ic_category_fruits
                    // Vegetables
                    4 -> R.drawable.ic_category_vegetables
                    // Rice
                    5 -> R.drawable.ic_category_rice
                    // Others
                    6 -> R.drawable.ic_category_others
                    // Unknown
                    else -> R.drawable.ic_unknown
                }
            )

            // Change the category title
            categoryTitle.text = title

            categoryCard.setOnClickListener {
                val intent = Intent(
                    context, MarketNavActivity::class.java
                )

                intent.putExtra(
                    Constants.CURRENT_LOCATION,
                    LatLng(curLoc[0], curLoc[1])
                )

                intent.putExtra("category_title", title)

                intent.putExtra("category_code", position)

                context.startActivity(intent)
            }
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
