package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.geofire.GeoLocation
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.MarketAdapter
import edu.cccdci.opal.databinding.ActivityMarketNavBinding
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class MarketNavActivity : UtilityClass() {

    private lateinit var binding: ActivityMarketNavBinding
    private lateinit var marketAdapter: MarketAdapter
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private var mCategoryTitle: String? = null
    private var mCategoryDesc: String? = null
    private var mCategoryCode: Int? = null
    private var mCurLocJson: String? = null
    private var mUserLocation: CurrentLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMarketNavBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        // Check if there's an existing category title data
        if (intent.hasExtra(Constants.CATEGORY_TITLE))
            mCategoryTitle = intent.getStringExtra(Constants.CATEGORY_TITLE)

        // Check if there's an existing category description data
        if (intent.hasExtra(Constants.CATEGORY_DESC))
            mCategoryDesc = intent.getStringExtra(Constants.CATEGORY_DESC)

        // Check if there's an existing category code data
        if (intent.hasExtra(Constants.CATEGORY_CODE))
            mCategoryCode = intent.getIntExtra(Constants.CATEGORY_CODE, -1)

        // Get the current location in String JSON format
        mCurLocJson = mSharedPrefs.getString(Constants.CURRENT_LOCATION, "")

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMarketNavActivity, false)

            // Store the user's location from String JSON format
            if (!mCurLocJson.isNullOrEmpty())
                mUserLocation = Gson().fromJson(mCurLocJson, CurrentLocation::class.java)

            // Set the market navigation title
            tvMarketNavTitle.text = mCategoryTitle
                ?: getString(R.string.market_navigation_title)

            tvMarketNavDesc.text = mCategoryDesc
                ?: getString(R.string.market_navigation_desc)

            // Set the user's current address
            tvUserCurrentAddress.text = mUserLocation?.fullAddress
                ?: getString(R.string.user_current_location)

            // Get all nearby markets using the current location
            FirestoreClass().getNearbyLocations(
                this@MarketNavActivity,
                GeoLocation(
                    mUserLocation?.latitude ?: 0.0,
                    mUserLocation?.longitude ?: 0.0
                )
            )
        }  // end of with(binding)

    }  // end of onCreate method

    // Function to display the list of nearby markets
    internal fun getRetrievedMarkets(markets: List<Market>) {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvMarketNavResult.layoutManager = WrapperLinearLayoutManager(
                this@MarketNavActivity, LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Market Adapter
            marketAdapter = MarketAdapter(
                this@MarketNavActivity, markets,
                listOf(
                    mUserLocation?.latitude ?: 0.0,
                    mUserLocation?.longitude ?: 0.0
                )
            )

            // Sets the adapter of Markets RecyclerView
            rvMarketNavResult.adapter = marketAdapter
        }
    }  // end of getRetrievedMarkets method

}  // end of MarketNavActivity class
