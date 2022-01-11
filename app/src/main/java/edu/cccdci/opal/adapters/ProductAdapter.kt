package edu.cccdci.opal.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.ui.activities.ProductDescActivity
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader

class ProductAdapter(
    val context: Context,
    options: FirestoreRecyclerOptions<Product>
) : FirestoreRecyclerAdapter<Product, ProductAdapter.ProductViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from product (Home) item layout
        private val name: TextView = itemView.findViewById(R.id.tv_product_name)
        private val price: TextView = itemView.findViewById(R.id.tv_product_price)
        private val market: TextView = itemView.findViewById(R.id.tv_product_market)
        private val image: ImageView = itemView.findViewById(R.id.iv_product_home_image)
        private val productCard: CardView = itemView.findViewById(R.id.cv_product)

        internal fun setProductData(product: Product) {
            // Store the productData values in the respective views
            name.text = product.name
            price.text = context.getString(
                R.string.product_price, product.price, product.unit
            )
            market.text = product.market[Constants.NAME]

            // Load the product image
            GlideLoader(context).loadPicture(product.image, image)

            // Actions when the product card is clicked
            productCard.setOnClickListener {
                // Create an Intent to launch ProductDescActivity
                val intent = Intent(
                    context, ProductDescActivity::class.java
                )
                // Add product information to intent
                intent.putExtra(Constants.PRODUCT_DESCRIPTION, product)

                // Opens the edit user profile
                context.startActivity(intent)
            }  // end of setOnClickListener

        }  // end of setProductData method

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
    override fun onBindViewHolder(
        holder: ProductViewHolder, position: Int, product: Product
    ) {
        // Sets the values of market data to the current view (Home)
        holder.setProductData(product)
    }  // end of onBindViewHolder method

}  // end of ProductAdapter class