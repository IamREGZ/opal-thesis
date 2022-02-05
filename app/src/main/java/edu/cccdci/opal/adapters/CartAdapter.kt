package edu.cccdci.opal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.CartItem
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.utils.GlideLoader

class CartAdapter(
    private val activity: CartActivity,
    private val context: Context,
    private val cartDataList: MutableList<CartItem>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Get the subtotal of all cart item prices
    private var cartSubtotal: Double = if (cartDataList.isNotEmpty())
        cartDataList.sumOf { it.prodPrice }
    else
        0.0

    // Initialize a list for storing product details, to be used later on checkout
    private val cartItemDetails = if (itemCount > 0)
        arrayOfNulls<Product?>(itemCount).toMutableList()
    else
        mutableListOf()

    // Nested Class to hold views from the target layout
    inner class CartViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from cart item layout
        private val cartProdName: TextView = itemView
            .findViewById(R.id.tv_cart_prod_name)
        private val cartProdPrice: TextView = itemView
            .findViewById(R.id.tv_cart_prod_price)
        private val cartProdWeight: TextView = itemView
            .findViewById(R.id.tv_cart_prod_weight)
        private val cartProdQty: TextView = itemView
            .findViewById(R.id.tv_cart_qty_counter)
        private val cartTotalPrice: TextView = itemView
            .findViewById(R.id.tv_cart_prod_total_price)
        private val minusBtn: Button = itemView
            .findViewById(R.id.btn_cart_qty_minus)
        private val plusBtn: Button = itemView
            .findViewById(R.id.btn_cart_qty_plus)
        private val cartProdImage: ImageView = itemView
            .findViewById(R.id.iv_cart_prod_image)

        // The price of the current cart item
        private var price: Double = 0.00

        // Function to provide the product details in the current cart item
        internal fun setProductDetails(product: Product, position: Int) {
            // Put the retrieved product information in the list
            cartItemDetails[position] = product

            price = product.price  // Set the product price value

            // Supply the following text views
            cartProdName.text = product.name
            cartProdPrice.text = context.getString(
                R.string.product_price, price, product.unit
            )
            cartProdWeight.text = context.getString(
                R.string.product_weight, product.weight
            )

            // Add also the product image
            GlideLoader(context).loadImage(product.image, cartProdImage)

            // Then set the dynamic cart data values
            setCartData(cartDataList[position], position)
        }  // end of setProductDetails method

        // Function to set the dynamic values of the current cart item
        private fun setCartData(cartItem: CartItem, position: Int) {
            // Supply the following text view
            cartProdQty.text = cartItem.prodQTY.toString()
            cartTotalPrice.text = context.getString(
                R.string.item_price, price * cartItem.prodQTY
            )

            // Add decrease QTY button functionality
            minusBtn.setOnClickListener {
                changeQTY(position, price, cartProdQty, cartTotalPrice)
            }

            // Add increase QTY button functionality
            plusBtn.setOnClickListener {
                changeQTY(position, price, cartProdQty, cartTotalPrice, true)
            }
        }  // end of setCartData method

    }  // end of CartViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cart_item, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // Get an object from the current position of cart item list
        val cartItem = cartDataList[position]

        // Retrieve the current item's product based on the product ID
        FirestoreClass().retrieveProductItem(context, holder, position, cartItem.prodID)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = cartDataList.size

    // Function to change the QTY of the cart item
    private fun changeQTY(
        position: Int, price: Double, qty: TextView, totalPrice: TextView,
        toAdd: Boolean = false
    ) {
        // toAdd == true, add QTY; otherwise, subtract QTY
        if (toAdd) cartDataList[position].prodQTY++
        else cartDataList[position].prodQTY--

        // Change the current total price of the cart item
        cartDataList[position].prodPrice = price * cartDataList[position].prodQTY

        /* Change the texts of QTY and total price (prices on the right
         * side of the screen)
         */
        qty.text = cartDataList[position].prodQTY.toString()
        totalPrice.text = context.getString(
            R.string.item_price, cartDataList[position].prodPrice
        )

        // If the quantity reaches 0, remove the item from the cart
        if (cartDataList[position].prodQTY == 0) {
            cartDataList.removeAt(position)
            cartItemDetails.removeAt(position)
        }

        // To reflect changes made in the underlying dataset
        notifyDataSetChanged()

        // Update the current subtotal of all cart items
        cartSubtotal = cartDataList.sumOf { it.prodPrice }

        // Exit Cart Activity if all items are cleared
        if (cartDataList.isEmpty()) {
            activity.updateCart()  // Proceed to clear the cart data
            activity.finish()  // Close the Cart Activity
        } else {
            // Change the subtotal text outside the RecyclerView
            activity.setSubtotalValues(cartSubtotal)
        }  // end of if-else

    }  // end of changeQTY method

    // Function to get the subtotal of all cart items
    fun getSubtotal(): Double = cartSubtotal

    // Function to get the list of product details in the cart for checkout
    fun getProductDetails(): Array<Product?> = cartItemDetails.toTypedArray()

}  // end of CartAdapter class