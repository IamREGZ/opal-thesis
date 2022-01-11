package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityProductDescBinding
import edu.cccdci.opal.dataclasses.CartItem
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class ProductDescActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityProductDescBinding
    private var mUserInfo: User? = null
    private var mProdInfo: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductDescBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbProdDescActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.PRODUCT_DESCRIPTION)) {
                mProdInfo = intent.getParcelableExtra(Constants.PRODUCT_DESCRIPTION)!!

                if (mProdInfo != null) {
                    with(mProdInfo!!) {
                        // Stores the details of product
                        tvPdName.text = name
                        tvPdPrice.text = getString(
                            R.string.product_price, price, unit
                        )
                        tvPdDesc.text = description
                        tvPdMarket.text = market[Constants.NAME]

                        // Load the product image
                        GlideLoader(this@ProductDescActivity).loadPicture(
                            image, ivPdImage
                        )
                    }  // end of with(mProdInfo)
                }  // end of if

            }  // end of if

            // Gets the user profile data
            FirestoreClass().getUserDetails(this@ProductDescActivity)

            // Click event for Add to Cart Button
            btnAddToCart.setOnClickListener(this@ProductDescActivity)
            // Click event for Go to Cart Button
            btnGoToCart.setOnClickListener(this@ProductDescActivity)

        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Add the specific product to the cart when this button is clicked
                R.id.btn_add_to_cart -> {
                    // Prevents NPE
                    if (mUserInfo != null)
                        addToCart()  // Proceed to add to cart
                }

                // Go to Cart Activity when this button is clicked
                R.id.btn_go_to_cart -> {
                    // Create an Intent to launch CartActivity
                    val intent = Intent(
                        this@ProductDescActivity, CartActivity::class.java
                    )
                    // Add extra user information to intent
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                    startActivity(intent)  // Opens the user cart activity
                    finish()  // Closes the current activity
                }
            }  // end of when
        }  // end of if

    }  // end of onClick method

    // Function to store user details, as well as the status of the product
    fun setCurrentUserDetails(user: User) {
        mUserInfo = user  // Store the user details

        // Check if mUserInfo and cart property are not null to prevent NPE
        if (mUserInfo != null && mUserInfo!!.cart != null) {
            // Variable to store the result of product's presence in the user's cart
            var foundCartItem = false

            for (ctIt in mUserInfo!!.cart!!.cartItems) {
                // If a product is found in the user's cart, change the button
                if (ctIt.prodID == mProdInfo!!.id) {
                    foundCartItem = true
                    break
                }
            }  // end of for

            // Change the call-to-action button depending on the result
            changeCTAButton(foundCartItem)
        }  // end of if

    }  // end of setCurrentUserDetails method

    // Function to add the product to cart
    private fun addToCart() {
        // Display the loading message
        showProgressDialog(
            this@ProductDescActivity, this@ProductDescActivity,
            resources.getString(R.string.msg_please_wait)
        )

        // Check if mUserInfo and mProdInfo objects are not null to prevent NPE
        if (mUserInfo != null && mProdInfo != null) {
            // Update user's cart in the Firestore database
            FirestoreClass().updateCart(
                this@ProductDescActivity, mUserInfo!!,
                mProdInfo!!.market[Constants.ID]!!,
                listOf(CartItem(mProdInfo!!.id, 1, mProdInfo!!.price)),
                true
            )
        }
    }  // end of addToCart method

    // Function to prompt that the product has added to cart
    fun itemAddedPrompt() {
        // Gets the user profile data
        FirestoreClass().getUserDetails(this@ProductDescActivity)

        hideProgressDialog()  // Hide the loading message

        // Display the Toast message
        toastMessage(
            this@ProductDescActivity,
            resources.getString(R.string.msg_added_to_cart)
        )
    }  // end of itemAddedPrompt method

    /* Function to change call-to-action button depending on the item's
     * availability in the user's cart
     */
    private fun changeCTAButton(isInCart: Boolean) {
        with(binding) {
            if (isInCart) {
                // Make Go To Cart visible instead of Add To Cart
                btnGoToCart.visibility = View.VISIBLE
                btnAddToCart.visibility = View.GONE
            } else {
                // Make Add To Cart visible instead of Go To Cart
                btnAddToCart.visibility = View.VISIBLE
                btnGoToCart.visibility = View.GONE
            }
        }  // end of with(binding)
    }  // end of changeCTAButton method

}  // end of ProductDescActivity class