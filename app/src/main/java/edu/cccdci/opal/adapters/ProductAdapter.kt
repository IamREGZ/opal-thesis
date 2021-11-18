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
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.ui.activities.ProductDescActivity
import edu.cccdci.opal.utils.Constants

class ProductAdapter(
    val context: Context,
    private val productDataList: MutableList<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Nested Class to hold views from the target layout
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val market: TextView = itemView.findViewById(R.id.product_market)
        val productCard: CardView = itemView.findViewById(R.id.cv_product)
    }  // end of ProductViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.product_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        // Get the object from the current position of the data list
        val productData = productDataList[position]
        val user = User()  // Might be temporary

        with(holder) {
            // Store the productData values in the respective views
            name.text = productData.name
            price.text = "₱${productData.price} / kg"
            market.text = productData.market

            // Actions when the product card is clicked
            productCard.setOnClickListener {
                // Create an Intent to launch ProductDescActivity
                val intent = Intent(
                    context, ProductDescActivity::class.java
                )
                // Add product information to intent (might be temporary)
                intent.putExtra(Constants.PRODUCT_DESCRIPTION, productData)
                intent.putExtra(Constants.EXTRA_USER_INFO, user)

                // Opens the edit user profile
                context.startActivity(intent)
            }  // end of setOnClickListener

        }  // end of with(holder)

    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = productDataList.size

}  // end of ProductAdapter class