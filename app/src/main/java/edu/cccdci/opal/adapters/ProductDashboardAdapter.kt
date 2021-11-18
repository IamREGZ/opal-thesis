package edu.cccdci.opal.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.ui.activities.ProductEditorActivity
import edu.cccdci.opal.utils.Constants

class ProductDashboardAdapter(
    val context: Context,
    private val productDBDataList: MutableList<Product>
) : RecyclerView.Adapter<ProductDashboardAdapter.ProductDashboardViewHolder>() {

    // Nested Class to hold views from the target layout
    class ProductDashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.prod_db_name)
        val price: TextView = itemView.findViewById(R.id.prod_db_price)
        val productCard: CardView = itemView.findViewById(R.id.cv_product_db)
    }  // end of ProductDashboardViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProductDashboardViewHolder {
        return ProductDashboardViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.product_dashboard_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: ProductDashboardViewHolder, position: Int) {
        // Get the object from the current position of the data list
        val productData = productDBDataList[position]

        with(holder) {
            // Store the productData values in the respective views
            name.text = productData.name
            price.text = "₱${productData.price} / kg"

            // Actions when the product card is clicked
            productCard.setOnClickListener {
                // Create an Intent to launch ProductDescActivity
                val intent = Intent(
                    context, ProductEditorActivity::class.java
                )
                // Add product information to intent (might be temporary)
                intent.putExtra(Constants.PRODUCT_DESCRIPTION, productData)

                // Opens the edit user profile
                context.startActivity(intent)
            }

        }  // end of with(holder)

    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = productDBDataList.size

}  // end of ProductDashboardAdapter class