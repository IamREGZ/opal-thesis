package edu.cccdci.opal.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.databinding.ActivityCartBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.UtilityClass

class CartActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var mUserInfo: User? = null
    private var mMarket: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityCartBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMyCartActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                // Get data from the parcelable class
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)

                /* Check if mUserInfo and its cart property are not null
                 * to prevent NPE. Also, execute the retrieval process if
                 * market ID is set.
                 */
                if (mUserInfo != null && mUserInfo!!.cart != null &&
                    mUserInfo!!.cart!!.marketID.isNotEmpty()
                ) {
                    // Display the loading message
                    showProgressDialog(
                        this@CartActivity, this@CartActivity,
                        getString(R.string.msg_please_wait)
                    )

                    // Retrieve the market information from Firestore
                    FirestoreClass().retrieveMarket(
                        this@CartActivity, mUserInfo!!.cart!!.marketID
                    )
                } else {
                    // Make the empty cart layout visible
                    llEmptyCart.visibility = View.VISIBLE
                }
            }  // end of if

            // Click event for Checkout Button
            btnCheckout.setOnClickListener(this@CartActivity)
            // Click event for Continue Browsing Button
            btnContBrowse.setOnClickListener(this@CartActivity)
            // Click event for Delete All Cart Items ImageView
            ivCartDeleteAll.setOnClickListener(this@CartActivity)
            // Click event for Select All Cart Items CheckBox
            cbCartSelectAll.setOnClickListener(this@CartActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when the user presses back to exit the activity
    override fun onBackPressed() {
        super.onBackPressed()
        updateCart()  // Update the cart items before exiting
    }  // end of onBackPressed method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Sends user to Items Checkout
                R.id.btn_checkout -> {
                    updateCart()  // Update the cart items before going to checkout

                    // Check if mUserInfo and its cart property are not null
                    if (mUserInfo != null && mUserInfo!!.cart != null) {
                        // Create an intent to go to Checkout Activity
                        Intent(
                            this@CartActivity, CheckoutActivity::class.java
                        ).apply {
                            // Add the required information to intent
                            putExtra(Constants.EXTRA_USER_INFO, mUserInfo)
                            putExtra(Constants.MARKET_INFO, mMarket)
                            putExtra(
                                Constants.CART_PRODUCT_DETAILS,
                                cartAdapter.getProductDetails()
                            )

                            startActivity(this)  // Opens the activity
                        }  // end of run
                    }  // end of if
                }

                // Sends user back to the previous activity
                R.id.btn_cont_browse -> finish()

                // Remove all selected items from the cart
                R.id.iv_cart_delete_all -> DialogClass(
                    this@CartActivity, mAdapter = cartAdapter
                ).alertDialog(
                    getString(R.string.dialog_remove_selected_items_title),
                    getString(R.string.dialog_remove_selected_items_message),
                    getString(R.string.dialog_btn_remove),
                    getString(R.string.dialog_btn_cancel)
                )

                // Selects all items from the cart
                R.id.cb_cart_select_all -> cartAdapter.toggleAllSelection(
                    binding.cbCartSelectAll.isChecked
                )
            }  // end of when
        }  // end of if

    }  // end of onClick method

    // Function to store market name and its delivery fee in the market activity
    internal fun setMarketData(market: Market?) {
        // Prevents NullPointerException
        if (market != null) {
            mMarket = market  // Store the object for parcelable
            // Change the market name text view
            binding.tvCartMarketName.text = mMarket!!.name
        }

        setupCartAdapter()  // Setup the Cart RecyclerView Adapter
    }  // end of setMarketData method

    // Function to setup the RecyclerView adapter for cart
    private fun setupCartAdapter() {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvCart.layoutManager = WrapperLinearLayoutManager(
                this@CartActivity, LinearLayoutManager.VERTICAL, false
            )
            // Create an object of Cart Adapter
            cartAdapter = CartAdapter(
                this@CartActivity, this@CartActivity,
                mUserInfo!!.cart!!.cartItems
            )
            cartAdapter.apply {
                rvCart.adapter = this  // Sets the adapter of Cart RecyclerView

                // Update the total price, total weight, and number of selected items
                updateCartValues(getTotalPrice(), getTotalWeight(), getSelectedItems())
            }

            // Make the Cart Layout visible if cartItems is not empty
            if (mUserInfo!!.cart!!.cartItems.isNotEmpty())
                llCartLayout.visibility = View.VISIBLE
            else
                llEmptyCart.visibility = View.VISIBLE

            hideProgressDialog()  // Hide the loading message
        }  // end of with(binding)

    }  // end of setupCartAdapter method

    // Function to change all attributes in the cart (total price, weight, etc.)
    @SuppressLint("ResourceType")
    internal fun updateCartValues(price: Double, wt: Double, selected: Int) {
        with(binding) {
            // Change the total weight and total price values
            tvCartTotalWeight.text = getString(R.string.product_weight, wt)
            tvCartTotalPrice.text = getString(R.string.item_price, price)

            // Make the Select All CheckBox checked if all items are selected
            cbCartSelectAll.isChecked = selected == cartAdapter.itemCount

            // Weight limit banner will be visible if weight is greater than 25
            llWeightLimitBanner.visibility = if (wt > 25)
                View.VISIBLE
            else
                View.GONE

            // Total weight value text will be red if greater than 25. Else, black.
            tvCartTotalWeight.setTextColor(
                Color.parseColor(
                    getString(
                        if (wt > 25) R.color.colorErrorMessage else R.color.app_black
                    )
                )
            )

            // Delete all icon will be visible if there are any selected items
            ivCartDeleteAll.visibility = if (selected > 0) View.VISIBLE else View.INVISIBLE

            /* It enables if and only if weight is between 0 and 25, and at least
             * one item is selected from the cart
             */
            btnCheckout.isEnabled = wt in 0.0..25.0 && selected > 0
        }  // end of with(binding)

    }  // end of updateCartValues method

    // Function to update cart items upon activity exit
    internal fun updateCart() {
        // Check if mUserInfo and its cart property are not null to prevent NPE
        if (mUserInfo != null && mUserInfo!!.cart != null) {
            /* To determine if the market ID needs to be cleared depending
             * on the capacity of the list. True if cartItems is empty.
             */
            val clearCart: Boolean = mUserInfo!!.cart!!.cartItems.isEmpty()

            // Update the cart data in Firestore once the user exits the cart activity
            FirestoreClass().updateCart(
                this@CartActivity, mUserInfo!!.cart!!.cartItems,
                toClear = clearCart
            )
        }  // end of if
    }  // end of updateCart method

}  // end of CartActivity class
