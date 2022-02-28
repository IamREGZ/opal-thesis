package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductAdapter
import edu.cccdci.opal.databinding.ActivityMarketPageBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperGridLayoutManager
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class MarketPageActivity : UtilityClass() {

    private lateinit var binding: ActivityMarketPageBinding
    private var productAdapter: ProductAdapter? = null
    private var mMarketInfo: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMarketPageBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMarketPageActivity, false)

            // Check if there's an existing string extra info
            if (intent.hasExtra(Constants.MARKET_INFO)) {
                // Get the string data from intent
                mMarketInfo = intent.getParcelableExtra(Constants.MARKET_INFO)

                setUserMarket()
            }

            // Create a Builder for FirestoreRecyclerOptions
            val options = FirestoreRecyclerOptions.Builder<Product>()
                // Gets all the documents from products collection
                .setQuery(
                    FirestoreClass().getProductQuery(
                        null, this@MarketPageActivity, mMarketInfo!!.id
                    ),
                    Product::class.java
                )
                .build()

            // Sets the layout type of the RecyclerView
            rvMarketProfileProducts.layoutManager = WrapperGridLayoutManager(
                this@MarketPageActivity, 2, GridLayoutManager.VERTICAL, false
            )

            // Create an object of Product Adapter
            productAdapter = ProductAdapter(
                this@MarketPageActivity, options
            )

            // Sets the adapter of Home RecyclerView
            rvMarketProfileProducts.adapter = productAdapter

            // Actions when the more menu button in the user's market is clicked
            ivMarketProfileMoreMenu.setOnClickListener { view ->
                showMarketMenu(view)  // Open a popup menu
            }
        }

    }  // end of onCreate method

    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on products
        productAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        // Stops listening to Firestore operations on products
        if (productAdapter != null)
            productAdapter!!.stopListening()
    }

    // Function to change the output values of user's market
    private fun setUserMarket() {
        // Prevents NullPointerException
        if (mMarketInfo != null) {
            with(binding) {
                // Store the text values
                tvMarketProfileName.text = mMarketInfo!!.name

                // And then load the user's market image
                GlideLoader(this@MarketPageActivity).loadImage(
                    mMarketInfo!!.image, ivMarketProfileImage
                )
            }  // end of with(binding)

        }  // end of if

    }  // end of setUserMarket method

    // Function to show the popup menu when the more button is clicked
    private fun showMarketMenu(view: View) {
        // Prepare the popup menu object
        val popup = PopupMenu(this@MarketPageActivity, view)
        popup.menuInflater.inflate(R.menu.market_prof_menu, popup.menu)

        popup.menu.findItem(R.id.popup_mkt_edit_market).isVisible = false

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

            else -> null  // Return a null value
        }  // end of when

        if (targetClass != null) {
            /* Create an Intent to launch a target class, depending on the selected
             * menu item. Make sure targetClass is not null to avoid NPE.
             */
            Intent(this@MarketPageActivity, targetClass).let {
                // Add market information to intent
                it.putExtra(Constants.MARKET_INFO, mMarketInfo)

                startActivity(it)  // Opens the target class
            }
        } else {
            // Send back to Main Activity if targetClass is null
            Intent(this@MarketPageActivity, MainActivity::class.java).let {
                // Clear all activity layers
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(it)  // Opens the Main Activity
                finish()  // Closes this activity
            }
        }  // end of if-else

    }  // end of navigateMarket method

}  // end of MarketPageActivity