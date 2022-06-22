package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMyMarketDetailsBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class MyMarketDetailsActivity : UtilityClass(), OnMapReadyCallback {

    private lateinit var binding: ActivityMyMarketDetailsBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private var mMarketInfo: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMyMarketDetailsBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbUserMarketDetailsActivity, false)

            // Prepare the SupportMapFragment
            mSupportMap = supportFragmentManager
                .findFragmentById(R.id.mpfr_market_address) as SupportMapFragment

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.MARKET_INFO)) {
                // Get the parcelable data from intent
                mMarketInfo = intent.getParcelableExtra(Constants.MARKET_INFO)

                // Change the respective values in the layout
                setAdditionalMarketDetails()
            }  // end of if
        }  // end of with(binding)

    }  // end of onCreate method

    // Overriding function to set the Map UI of current delivery address
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        // Get the latitude and longitude of user's market
        val marketCoor = Constants.getLocation(mMarketInfo)
        // Create an object of user's market's latitude and longitude
        val marketLoc = LatLng(marketCoor[0], marketCoor[1])

        // Set the home marker attributes
        val marketMarker = MarkerOptions()
            // Set the position to the latitude and longitude of user's market
            .position(marketLoc)
            // Set the market title
            .title(if (mMarketInfo != null) mMarketInfo!!.name else null)
            // Customize the icon image
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_primary))

        mGoogleMap.apply {
            // Make the marker visible to the Map UI
            addMarker(marketMarker)
            // Focus the Map UI to the position of marker
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    marketLoc, Constants.ZOOM_LARGE
                )
            )

            // Disable all touch interactions and controls of the Map UI
            uiSettings.setAllGesturesEnabled(false)
            uiSettings.isMapToolbarEnabled = false
        }  // end of apply

    }  // end of onMapReady method

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
                // Format: Others (Specified Category)
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
                R.string.market_address_detailed, mMarketInfo!!.wetMarket,
                mMarketInfo!!.detailAdd, mMarketInfo!!.barangay,
                mMarketInfo!!.city, mMarketInfo!!.province,
                mMarketInfo!!.postal
            )

            // Market Delivery Fee
            tvMktDetDelivery.text = getString(
                R.string.item_price, mMarketInfo!!.deliveryFee
            )

            // Load the map fragment
            mSupportMap.getMapAsync(this@MyMarketDetailsActivity)
        }  // end of with(binding)

    }  // end of setAdditionalMarketDetails method

}  // end of MyMarketDetailsActivity class
