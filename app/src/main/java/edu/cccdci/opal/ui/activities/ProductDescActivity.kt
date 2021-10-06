package edu.cccdci.opal.ui.activities

import android.os.Bundle
import android.widget.Toast
import edu.cccdci.opal.databinding.ActivityProductDescBinding
import edu.cccdci.opal.dataclasses.Cart
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.utils.Constants

class ProductDescActivity : TemplateActivity() {

    private lateinit var binding: ActivityProductDescBinding
    private lateinit var mUserInfo: User
    private lateinit var mProdInfo: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDescBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            //Setups the Action Bar of the current activity
            setupActionBar(tlbProdDescActivity, false)

            //Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.PRODUCT_DESCRIPTION)) {
                mProdInfo = intent.getParcelableExtra(Constants.PRODUCT_DESCRIPTION)!!
            }

            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)!!
            }

            with(mProdInfo) {
                tvPdName.text = name
                tvPdPrice.text = "₱$price / kg"
                tvPdMarket.text = market

                btnAddToCart.setOnClickListener {
                    mUserInfo.cartItems.add(
                        Cart(name = name, price = price)
                    )
                    Toast.makeText(
                        this@ProductDescActivity,
                        "Added to cart successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)
                    finish()
                }

            }
        }
    }
}