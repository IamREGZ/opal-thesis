package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.MarketAdapter
import edu.cccdci.opal.databinding.ActivityMarketNavBinding
import edu.cccdci.opal.dataclasses.Address
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
    private var mCurrentLocation: LatLng? = null
    private var mCategoryTitle: String? = null
    private var mCategoryCode: Int? = null
    private var mSelectedLocAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMarketNavBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        if (intent.hasExtra(Constants.CURRENT_LOCATION)) {
            mCurrentLocation = intent.getParcelableExtra(Constants.CURRENT_LOCATION)
        }

        if (intent.hasExtra("category_title")) {
            mCategoryTitle = intent.getStringExtra("category_title")!!
        }

        if (intent.hasExtra("category_code")) {
            mCategoryCode = intent.getIntExtra("category_code", 6)
        }

        val selectedLocation = mSharedPrefs.getString(
            Constants.CURRENT_ADDRESS_DETAILS, ""
        )!!

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMarketNavActivity, false)

            tvMarketNavTitle.text = mCategoryTitle!!

            if (selectedLocation.isNotEmpty()) {
                mSelectedLocAddress = Gson().fromJson(
                    selectedLocation, Address::class.java
                )

                tvUserCurrentAddress.text = getString(
                    R.string.current_location,
                    mSelectedLocAddress!!.detailAdd,
                    mSelectedLocAddress!!.barangay,
                    mSelectedLocAddress!!.city,
                    mSelectedLocAddress!!.province,
                    mSelectedLocAddress!!.postal
                )
            }

            FirestoreClass().retrieveNearMarkets(
                this@MarketNavActivity,
                GeoLocation(
                    mCurrentLocation!!.latitude,
                    mCurrentLocation!!.longitude
                )
            )
        }  // end of with(binding)

    }  // end of onCreate method

    fun getRetrievedMarkets(markets: List<Market>) {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvMarketNavResult.layoutManager = WrapperLinearLayoutManager(
                this@MarketNavActivity, LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Market Adapter
            marketAdapter = MarketAdapter(
                this@MarketNavActivity, markets,
                listOf(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
            )

            // Sets the adapter of Markets RecyclerView
            rvMarketNavResult.adapter = marketAdapter
        }
    }

}  // end of MarketNavActivity