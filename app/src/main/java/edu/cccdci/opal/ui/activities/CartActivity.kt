package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.databinding.ActivityCartBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class CartActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var mUserInfo: User? = null
    private var mMarket: Market? = null
    private var mSubtotal: Double = 0.0
    private var mDelivery: Double = 0.0
    private var mTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
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
                        resources.getString(R.string.msg_please_wait)
                    )

                    // Retrieve the market information from Firestore
                    FirestoreClass().retrieveMarket(
                        this@CartActivity, mUserInfo!!.cart!!.marketID
                    )
                }  // end of if
            }  // end of if

            // Click event for Checkout Button
            btnCheckout.setOnClickListener(this@CartActivity)
            // Click event for Continue Browsing Button
            btnContBrowse.setOnClickListener(this@CartActivity)
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
                        val intent = Intent(
                            this@CartActivity, CheckoutActivity::class.java
                        )
                        // Add the required information to intent
                        intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)
                        intent.putExtra(Constants.MARKET_INFO, mMarket)
                        intent.putExtra(
                            Constants.CART_PRODUCT_DETAILS,
                            cartAdapter.getProductDetails()
                        )

                        startActivity(intent)  // Opens the activity
                    }  // end of if
                }

                // Sends user back to the previous activity
                R.id.btn_cont_browse -> finish()
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Function to store market name and its delivery fee in the market activity
    fun setMarketData(market: Market?) {
        // Prevents NullPointerException
        if (market != null) {
            mMarket = market  // Store the object for parcelable

            // Change the market name text view
            binding.tvCartMarketName.text = mMarket!!.name
        }

        // Store the delivery fee value. Default is 0.0 if mMarket is null.
        mDelivery = if (mMarket != null) mMarket!!.deliveryFee else 0.0

        // Change the delivery fee text view
        binding.tvDeliveryFee.text = getString(R.string.item_price, mDelivery)

        setupCartAdapter()  // Setup the Cart RecyclerView Adapter

        // Update the subtotal and total values
        setSubtotalValues(cartAdapter.getSubtotal())
    }  // end of setMarketData method

    // Function to change the subtotal and total text views once there are changes made
    fun setSubtotalValues(sbt: Double) {
        mSubtotal = sbt  // Store the new subtotal value
        // Get the sum of subtotal and delivery fee
        mTotal = mSubtotal + mDelivery

        // Change the subtotal and total text views
        binding.tvSubTotal.text = getString(R.string.item_price, mSubtotal)
        binding.tvTotal.text = getString(R.string.item_price, mTotal)
    }  // end of setSubtotalValues method

    // Function to setup the RecyclerView adapter for cart
    private fun setupCartAdapter() {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvCart.layoutManager = object : LinearLayoutManager(
                this@CartActivity
            ) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false
            }
            // Create an object of Cart Adapter
            cartAdapter = CartAdapter(
                this@CartActivity, this@CartActivity,
                mUserInfo!!.cart!!.cartItems
            )
            // Sets the adapter of Cart RecyclerView
            rvCart.adapter = cartAdapter

            // Make the Cart Layout visible if cartItems is not empty
            if (mUserInfo!!.cart!!.cartItems.isNotEmpty()) {
                svCartLayout.visibility = View.VISIBLE
                llEmptyCart.visibility = View.GONE
            }

            hideProgressDialog()  // Hide the loading message
        }  // end of with(binding)

    }  // end of setupCartAdapter method

    // Function to update cart items upon activity exit
    fun updateCart() {
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