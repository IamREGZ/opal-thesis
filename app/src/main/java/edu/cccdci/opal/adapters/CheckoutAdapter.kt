package edu.cccdci.opal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.CartItem
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.utils.GlideLoader

class CheckoutAdapter(
    private val context: Context,
    private val cartItemList: List<CartItem>,
    private val cartDetailsList: List<Product>
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class CheckoutViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from checkout item layout
        private val chkProdName: TextView = itemView
            .findViewById(R.id.tv_chkout_prod_name)
        private val chkProdPrice: TextView = itemView
            .findViewById(R.id.tv_chkout_prod_price)
        private val chkProdQTY: TextView = itemView
            .findViewById(R.id.tv_chkout_prod_qty)
        private val chkTotalPrice: TextView = itemView
            .findViewById(R.id.tv_chkout_prod_total_price)
        private val chkProdImage: ImageView = itemView
            .findViewById(R.id.iv_chkout_prod_image)

        // Function to provide the product details in the current checkout item
        internal fun setItemData(cartItem: CartItem, product: Product) {
            // Supply the following text views
            chkProdName.text = product.name
            chkProdPrice.text = context.getString(
                R.string.product_price, product.price, product.unit
            )
            chkProdQTY.text = context.getString(
                R.string.product_qty, cartItem.prodQTY
            )
            chkTotalPrice.text = context.getString(
                R.string.item_price, cartItem.prodPrice
            )

            // Add also the product image
            GlideLoader(context).loadImage(product.image, chkProdImage)
        }  // end of setItemData method

    }  // end of CheckoutViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        return CheckoutViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_checkout, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        // Get an object from the current position of cart item list
        val cartItem = cartItemList[position]
        // Get an object from the current position of cart details list
        val cartDetail = cartDetailsList[position]

        // Sets the values of checkout item data to the current view
        holder.setItemData(cartItem, cartDetail)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = cartItemList.size

}  // end of CheckoutAdapter class
