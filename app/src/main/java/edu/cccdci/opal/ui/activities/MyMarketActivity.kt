package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMyMarketBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class MyMarketActivity : UtilityClass() {

    private lateinit var binding: ActivityMyMarketBinding
    private var mMarketID: String? = null
    private var mMarketInfo: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMyMarketBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMyMarketActivity, false)

            // Check if there's an existing string extra info
            if (intent.hasExtra(Constants.MARKET_ID_DATA)) {
                // Get the string data from intent
                mMarketID = intent.getStringExtra(Constants.MARKET_ID_DATA)
            }

            // Actions when the more menu button in the user's market is clicked
            ivUserMarketMoreMenu.setOnClickListener { view ->
                showMarketMenu(view)  // Open a popup menu
            }
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible
    override fun onStart() {
        super.onStart()

        // Prevents NullPointerException
        if (mMarketID != null) {
            // Display the loading message
            showProgressDialog(
                this@MyMarketActivity, this@MyMarketActivity,
                getString(R.string.msg_please_wait)
            )

            // Proceed to retrieve the user's market using the market's ID
            FirestoreClass().retrieveMarket(this@MyMarketActivity, mMarketID!!)
        }  // end of if

    }  // end of onStart method

    // Function to change the output values of user's market
    fun setUserMarket(market: Market?) {
        hideProgressDialog()  // Hide the loading message

        mMarketInfo = market  // Store the retrieved user's market object

        // Prevents NullPointerException
        if (mMarketInfo != null) {
            with(binding) {
                // Store the text values
                tvUserMarketName.text = mMarketInfo!!.name
                tvUserMarketProducts.text = mMarketInfo!!.products.toString()
                tvUserMarketProductSold.text = mMarketInfo!!.sold.toString()
                tvUserMarketVisits.text = mMarketInfo!!.visits.toString()
                tvUserMarketRating.text = getString(
                    R.string.rating_count, mMarketInfo!!.ratings
                )

                // And then load the user's market image
                GlideLoader(this@MyMarketActivity).loadImage(
                    mMarketInfo!!.image, ivUserMarketImage
                )
            }  // end of with(binding)

        }  // end of if

    }  // end of setUserMarket method

    // Function to show the popup menu when the more button is clicked
    private fun showMarketMenu(view: View) {
        // Prepare the popup menu object
        val popup = PopupMenu(this@MyMarketActivity, view)
        popup.menuInflater.inflate(R.menu.market_prof_menu, popup.menu)

        // Actions when the popup menu item is selected
        popup.setOnMenuItemClickListener { menuItem ->
            // Navigate using menu item ID
            navigateMarket(menuItem.itemId)
            true
        }  // end of setOnMenuItemClickListener

        popup.show()  // Display the popup menu
    }  // end of showMarketMenu method

    /* Function to navigate user from this activity to a destination activity
     * using menu item ID
     */
    private fun navigateMarket(menuItemID: Int) {
        // Variable to store class destination
        val targetClass = when (menuItemID) {
            // Sends user to the additional details of his/her market
            R.id.popup_mkt_more_details -> MyMarketDetailsActivity::class.java

            // Go to the Market Editor with the market data
            R.id.popup_mkt_edit_market -> MarketEditorActivity::class.java

            else -> null  // Return a null value
        }  // end of when

        if (targetClass != null) {
            /* Create an Intent to launch a target class, depending on the selected
             * menu item. Make sure targetClass is not null to avoid NPE.
             */
            Intent(this@MyMarketActivity, targetClass).let {
                // Add market information to intent
                it.putExtra(Constants.MARKET_INFO, mMarketInfo)

                startActivity(it)  // Opens the target class
            }
        } else {
            // Send back to Main Activity if targetClass is null
            Intent(this@MyMarketActivity, MainActivity::class.java).let {
                // Clear all activity layers
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(it)  // Opens the Main Activity
                finish()  // Closes this activity
            }
        }  // end of if-else

    }  // end of navigateMarket method

}  // end of MyMarketActivity class