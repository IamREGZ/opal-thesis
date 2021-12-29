package edu.cccdci.opal.ui.activities

import android.os.Bundle
import android.widget.Toast
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityProductDescBinding
import edu.cccdci.opal.dataclasses.Cart
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class ProductDescActivity : UtilityClass() {

    private lateinit var binding: ActivityProductDescBinding
    private lateinit var mUserInfo: User
    private lateinit var mProdInfo: Product

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductDescBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbProdDescActivity, false)

            // Check if there's an existing parcelable extra info (might be temporary)
            if (intent.hasExtra(Constants.PRODUCT_DESCRIPTION)) {
                mProdInfo = intent.getParcelableExtra(Constants.PRODUCT_DESCRIPTION)!!
            }

            // Check if there's an existing parcelable extra info (might be temporary)
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)!!
            }

            with(mProdInfo) {
                // Stores the details of product
                tvPdName.text = name
                tvPdPrice.text = getString(
                    R.string.product_price, price
                )
                tvPdMarket.text = market

                btnAddToCart.setOnClickListener {
                    // Temporary codes, might be deleted/modified
                    mUserInfo.cartItems.add(
                        Cart(name = name, price = price)
                    )
                    Toast.makeText(
                        this@ProductDescActivity,
                        "Added to cart successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)
                    finish()  // Closes the activity
                }  // end of setOnClickListener

            }  // end of with(mProdInfo)

        }  // end of with(binding)

    }  // end of onCreate method

}  // end of ProductDescActivity class