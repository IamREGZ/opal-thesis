package edu.cccdci.opal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.CartItem
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.GlideLoader

class CartAdapter(
    private val activity: CartActivity,
    private val context: Context,
    private val cartDataList: MutableList<CartItem>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Initialize a list for storing product details, to be used later on checkout
    private val cartItemDetails: MutableList<Product?> = MutableList(itemCount) { null }

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
        private val minusBtn: TextView = itemView
            .findViewById(R.id.tv_cart_qty_minus_btn)
        private val plusBtn: TextView = itemView
            .findViewById(R.id.tv_cart_qty_plus_btn)
        private val cartIsSelected: CheckBox = itemView
            .findViewById(R.id.cb_cart_item_selection)
        private val cartProdImage: ImageView = itemView
            .findViewById(R.id.iv_cart_prod_image)

        // The price of the current cart item
        private var price: Double = 0.00

        // The weight of the current cart item
        private var weight: Double = 0.00

        // Function to provide the product details in the current cart item
        internal fun setProductDetails(product: Product, position: Int) {
            // Put the retrieved product information in the list
            cartItemDetails[position] = product

            price = product.price  // Set the product price value
            weight = product.weight  // Set the product weight value

            // Supply the following text views
            cartProdName.text = product.name
            cartProdPrice.text = context.getString(
                R.string.product_price, price, product.unit
            )
            cartProdWeight.text = context.getString(
                R.string.product_weight, weight
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

            // Supply the current selection status of current cart item
            cartIsSelected.isChecked = cartItem.selected

            // Add decrease QTY button functionality
            minusBtn.setOnClickListener {
                changeQTY(position, price, weight, cartProdQty, cartTotalPrice)
            }

            // Add increase QTY button functionality
            plusBtn.setOnClickListener {
                changeQTY(position, price, weight, cartProdQty, cartTotalPrice, true)
            }

            // Event triggers when the checked status of cartIsSelected is changed
            cartIsSelected.setOnCheckedChangeListener { _, isSelected ->
                try {
                    // Store the current checked status
                    cartDataList[position].selected = isSelected

                    /* Update the subtotal, total weight, and number of selected items
                     * from the parent activity (CartActivity)
                     */
                    activity.updateCartValues(
                        getTotalPrice(), getTotalWeight(), getSelectedItems()
                    )
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()  // Log the error

                    // Refresh the whole dataset if IOOBE occurs
                    notifyItemRangeChanged(0, itemCount)
                }  // end of try-catch
            }
        }  // end of setCartData method

    }  // end of CartViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_cart, parent, false
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
    internal fun changeQTY(
        position: Int, price: Double, weight: Double, qty: TextView,
        totalPrice: TextView, toAdd: Boolean = false
    ) {
        // toAdd == true, add QTY; otherwise, subtract QTY (must be greater than 1)
        when {
            toAdd -> cartDataList[position].prodQTY++

            cartDataList[position].prodQTY > 1 -> cartDataList[position].prodQTY--

            // Display an alert dialog with two action buttons (Remove & Cancel)
            else -> DialogClass(context, mObj = position, mAdapter = this)
                .alertDialog(
                    context.getString(R.string.dialog_remove_from_cart_title),
                    context.getString(R.string.dialog_remove_from_cart_message),
                    context.getString(R.string.dialog_btn_remove),
                    context.getString(R.string.dialog_btn_cancel)
                )
        }  // end of when

        // Change the text values if the product QTY is greater than or equal to 1
        if (cartDataList[position].prodQTY >= 1) {
            // Change the current total price and weight of the cart item
            cartDataList[position].prodPrice = price * cartDataList[position].prodQTY
            cartDataList[position].prodWeight = weight * cartDataList[position].prodQTY

            /* Change the texts of QTY and total price (prices on the right
             * side of the screen)
             */
            qty.text = cartDataList[position].prodQTY.toString()
            totalPrice.text = context.getString(
                R.string.item_price, cartDataList[position].prodPrice
            )

            /* Update the subtotal, total weight, and number of selected items
             * from the parent activity (CartActivity)
             */
            activity.updateCartValues(
                getTotalPrice(), getTotalWeight(), getSelectedItems()
            )
        }  // end of if

    }  // end of changeQTY method

    // Function to remove certain item(s) from the cart
    internal fun removeItemsFromCart(position: Int? = null) {
        // For one item
        if (position != null) {
            // Remove the item
            cartDataList.removeAt(position)
            cartItemDetails.removeAt(position)

            // To reflect changes made in the underlying dataset
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, itemCount)
        }
        // For multiple items
        else {
            var i = 0  // Index
            var counter = getSelectedItems()  // Number of selected items

            // Perform the codes while the number of selected items is not equal to 0
            while (counter != 0) {
                /* Stops the loop if the index is greater than the last index
                 * (itemCount - 1)
                 */
                if (i >= itemCount) break

                // Remove the item if selected is true
                if (cartDataList[i].selected) {
                    // Remove the item
                    cartDataList.removeAt(i)
                    cartItemDetails.removeAt(i)

                    // To reflect changes made in the underlying dataset
                    notifyItemRemoved(i)
                    notifyItemRangeChanged(0, itemCount)

                    counter--  // Decrease the number of selected items
                    i = 0  // Go back to the starting index
                } else {
                    i++  // Proceed to the next index
                }
            }  // end of while
        }  // end of if-else

        // Check if the cart is empty
        if (cartDataList.isEmpty()) {
            activity.updateCart()  // Proceed to clear the cart data
            activity.finish()  // Close the Cart Activity
        } else {
            /* Update the subtotal, total weight, and number of selected items
             * from the parent activity (CartActivity)
             */
            activity.updateCartValues(
                getTotalPrice(), getTotalWeight(), getSelectedItems()
            )
        }
    }  // end of removeItemsFromCart method

    // Function to check (or uncheck) all items if the Select All Checkbox was ticked
    internal fun toggleAllSelection(isChecked: Boolean) {
        // Make sure it's not empty to prevent IndexOutOfBoundsException
        if (cartDataList.isNotEmpty()) {
            for (item in cartDataList.indices) {
                // Change the selected status to the opposite result
                if (cartDataList[item].selected != isChecked) {
                    cartDataList[item].selected = isChecked
                    notifyItemChanged(item)
                }
            }

            /* Update the subtotal, total weight, and number of selected items
             * from the parent activity (CartActivity)
             */
            activity.updateCartValues(
                getTotalPrice(), getTotalWeight(), getSelectedItems()
            )
        }
    }  // end of toggleAllSelection method

    // Function to get the number of selected cart items
    internal fun getSelectedItems(): Int = cartDataList.count { it.selected }

    // Function to get the total price of all cart items
    internal fun getTotalPrice(): Double = cartDataList.filter { it.selected }
        .sumOf { it.prodPrice }

    // Function to get the total weight of all cart items
    internal fun getTotalWeight(): Double = cartDataList.filter { it.selected }
        .sumOf { it.prodWeight }

    // Function to get the list of selected product details in the cart for checkout
    internal fun getProductDetails(): Array<Product> {
        // Stores all selected indices from cartDataList
        val selectedIndices: MutableList<Int> = mutableListOf()
        // Stores the results
        val productList: MutableList<Product> = mutableListOf()

        // Get all the indices from cartDataList where selected is true
        cartDataList.indices.forEach {
            if (cartDataList[it].selected) selectedIndices.add(it)
        }

        // Get all the products from cartItemDetails where prod is not null
        selectedIndices.forEach {
            cartItemDetails[it]?.let { prod -> productList.add(prod) }
        }

        return productList.toTypedArray()
    }  // end of getProductDetails method

}  // end of CartAdapter class
