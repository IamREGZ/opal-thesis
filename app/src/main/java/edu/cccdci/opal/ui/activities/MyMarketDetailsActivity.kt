package edu.cccdci.opal.ui.activities

import android.os.Bundle
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMyMarketDetailsBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class MyMarketDetailsActivity : UtilityClass() {

    private lateinit var binding: ActivityMyMarketDetailsBinding
    private var mMarketInfo: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMyMarketDetailsBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbUserMarketDetailsActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.MARKET_INFO)) {
                // Get the parcelable data from intent
                mMarketInfo = intent.getParcelableExtra(Constants.MARKET_INFO)

                // Prevents NullPointerException
                if (mMarketInfo != null) {
                    // Change the respective values in the layout
                    setAdditionalMarketDetails()
                }
            }  // end of if
        }  // end of with(binding)

    }  // end of onCreate method

    // Function to set the additional market details values
    private fun setAdditionalMarketDetails() {
        with(binding) {
            // Get the string array of market categories
            val categories = resources.getStringArray(
                R.array.market_categories_list
            )

            // Market Name
            tvMktDetName.text = mMarketInfo!!.name

            // Market Category
            tvMktDetCategory.text = if (
                categories[mMarketInfo!!.category] == Constants.ITEM_OTHERS
            ) {
                // Format: Others | Specified Category
                getString(
                    R.string.market_other_category,
                    categories[mMarketInfo!!.category],
                    mMarketInfo!!.otherCat
                )
            } else {
                // Format: Category only
                categories[mMarketInfo!!.category]
            }

            /* Market Address (Wet Market, Detailed Address, Barangay,
             * City/Municipality, Province & Postal Code)
             */
            tvMktDetAddress.text = getString(
                R.string.market_address, mMarketInfo!!.wetMarket,
                mMarketInfo!!.detailAdd, mMarketInfo!!.barangay,
                mMarketInfo!!.city, mMarketInfo!!.province,
                mMarketInfo!!.postal
            )

            // Market Delivery Fee
            tvMktDetDelivery.text = getString(
                R.string.item_price, mMarketInfo!!.deliveryFee
            )
        }  // end of with(binding)

    }  // end of setAdditionalMarketDetails method

}  // end of MyMarketDetailsActivity class