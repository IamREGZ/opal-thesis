package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.databinding.ActivityCartBinding
import edu.cccdci.opal.dataclasses.CartItem
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class CartActivity : UtilityClass() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var mCartItems: MutableList<CartItem> = mutableListOf()
    private var mUserInfo: User? = null
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

                // Check if mUserInfo and its cart property are not null to prevent NPE
                if (mUserInfo != null && mUserInfo!!.cart != null) {
                    // Display the loading message
                    showProgressDialog(
                        this@CartActivity, this@CartActivity,
                        resources.getString(R.string.msg_please_wait)
                    )

                    // Get the user's cart data from Firestore
                    mCartItems = mUserInfo!!.cart!!.cartItems

                    // Retrieve the market information from Firestore
                    FirestoreClass().retrieveMarket(
                        this@CartActivity, mUserInfo!!.cart!!.marketID
                    )
                }
            }  // end of if

            // Sets the layout type of the RecyclerView
            rvCart.layoutManager = object : LinearLayoutManager(this@CartActivity) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false
            }
            // Create an object of Cart Adapter
            cartAdapter = CartAdapter(
                this@CartActivity, this@CartActivity, mCartItems
            )
            // Sets the adapter of Cart RecyclerView
            rvCart.adapter = cartAdapter
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when the user presses back to exit the activity
    override fun onBackPressed() {
        super.onBackPressed()

        // Check if mUserInfo and its cart property are not null to prevent NPE
        if (mUserInfo != null && mUserInfo!!.cart != null) {
            // Update the cart data in Firestore once the user exits the cart activity
            FirestoreClass().updateCart(
                this@CartActivity, mUserInfo!!,
                mUserInfo!!.cart!!.marketID, cartAdapter.getCartItems()
            )
        }  // end of if
    }  // end of onBackPressed method

    // Function to store market name and its delivery fee in the market activity
    fun setMarketData(market: Market) {
        with(binding) {
            // Change the market name text view
            tvCartMarketName.text = market.name

            // Set the market's delivery fee data
            mDelivery = market.deliveryFee
            tvDeliveryFee.text = getString(R.string.cart_price, mDelivery)
        }  // end of with

        // Update the subtotal and total values
        setSubtotalValues(cartAdapter.getSubtotal())

        hideProgressDialog()  // Hide the loading message
    }  // end of setMarketData method

    // Function to change the subtotal and total text views once there are changes made
    fun setSubtotalValues(sbt: Double) {
        mSubtotal = sbt  // Store the new subtotal value
        mTotal = mSubtotal + mDelivery  // Get the sum of subtotal and delivery fee

        with(binding) {
            // Change the subtotal and total text views
            tvSubTotal.text = getString(R.string.cart_price, mSubtotal)
            tvTotal.text = getString(R.string.cart_price, mTotal)
        }  // end of with(binding)
    }  // end of setSubtotalValues method

}  // end of CartActivity class