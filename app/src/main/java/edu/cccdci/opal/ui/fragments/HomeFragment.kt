package edu.cccdci.opal.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.MarketBrowseAdapter
import edu.cccdci.opal.adapters.MarketCategoriesAdapter
import edu.cccdci.opal.databinding.FragmentHomeBinding
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.ui.activities.AddressesActivity
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.ui.activities.MainActivity
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.UtilityClass

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var browseAdapter: MarketBrowseAdapter
    private lateinit var categoriesAdapter: MarketCategoriesAdapter
    private lateinit var mFLPC: FusedLocationProviderClient
    private lateinit var mLocRequest: LocationRequest
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private lateinit var mUtility: UtilityClass
    private var mUserInfo: User? = null

    /* Variable to handle activity result contracts (inputs) and callbacks (outputs).
     * Specifically, it handles access permissions for fine (precise) location.
     */
    @SuppressLint("MissingPermission")
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // If the user grants fine location access permission
        if (isGranted) {
            // If the location services is enabled in the user's mobile device
            if (isLocationServicesEnabled()) {
                // Display the loading message
                mUtility.showProgressDialog(
                    requireContext(), requireActivity(),
                    getString(R.string.msg_please_wait)
                )
                // Execute location updates
                mFLPC.requestLocationUpdates(mLocRequest, mLocCallback, Looper.myLooper()!!)
            }
            // If not
            else {
                // Display an alert dialog with two action buttons (OK & Cancel)
                DialogClass(requireContext(), this@HomeFragment)
                    .alertDialog(
                        getString(R.string.dialog_enable_location_title),
                        getString(R.string.dialog_enable_location_message),
                        getString(R.string.dialog_btn_ok),
                        getString(R.string.dialog_btn_cancel)
                    )
            }  // end of if-else
        } else {
            // If the user denies the permission access
            mUtility.showSnackBar(
                requireActivity(),
                getString(R.string.err_fine_location_permission_denied),
                true
            )
        }  // end of if-else
    }  // end of registerForActivityResult

    // Variable to handle location callbacks (results) and execute some codes
    private val mLocCallback: LocationCallback = object : LocationCallback() {
        // Overriding function to execute codes with the resulting location
        override fun onLocationResult(result: LocationResult) {
            Constants.getDeviceLocation(
                requireActivity(), result.lastLocation, this@HomeFragment
            )
        }  // end of onLocationResult method
    }  // end of LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize the FusedLocationProviderClient
        mFLPC = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the Location Request object
        mLocRequest = LocationRequest.create().apply {
            // Get device location within 100 meter approximation
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = Constants.LOC_REQ_INTERVAL  // 10 mins
            fastestInterval = Constants.LOC_REQ_FASTEST_INTERVAL  // 5 mins
            maxWaitTime = Constants.LOC_REQ_MAX_WAIT_TIME  // 30 mins
            setExpirationDuration(Constants.LOC_REQ_EXP_DURATION)  // 2 mins
        }

        mUtility = UtilityClass()  // Initialize the utility class for this fragment

        // This is important to make two icons (my cart and messages) visible
        setHasOptionsMenu(true)
    }  // end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        // Creates the Shared Preferences
        mSharedPrefs = requireContext().getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        with(binding) {
            // Sets the layout attributes of Market Browse RecyclerView
            rvMarketBrowse.layoutManager = object : GridLayoutManager(
                requireContext(), 2, VERTICAL, false
            ) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false
            }

            // Sets the layout attributes of Market Categories RecyclerView
            rvMarketCategories.layoutManager = object : LinearLayoutManager(
                requireContext(), HORIZONTAL, false
            ) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false

                // Enable horizontal scroll functionality of the RecyclerView
                override fun canScrollHorizontally(): Boolean = true
            }

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Operations to do when this fragment is visible
    override fun onStart() {
        super.onStart()
        // Get the argument from the parent activity (MainActivity)
        val bundle = this.arguments

        /* Get the parcelable class (User) from the parent activity
         * (MainActivity)
         */
        if (bundle != null)
            mUserInfo = bundle.getParcelable(Constants.EXTRA_USER_INFO)

        setLocationSpinner()  // Set the location options spinner items
    }  // end of onStart method

    // Override the function to add two icons on top app bar (my cart and messages)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }  // end of onCreateOptionsMenu method

    // onOptionsItemSelected events are declared here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Send to Item Cart
            R.id.tab_cart -> {
                // Create an Intent to launch CartActivity
                Intent(requireContext(), CartActivity::class.java).run {
                    // Add product information to intent
                    putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                    // Opens the user cart activity
                    requireContext().startActivity(this)
                }  // end of run
            }

            // Send to Messages
            R.id.tab_messages -> findNavController().navigate(R.id.home_to_messages)
        }

        return super.onOptionsItemSelected(item)
    }  // end of onOptionsItemSelected method

    // Function to store the resulting location data to SharedPreferences
    internal fun storeLocationResult(locData: String?) {
        // Store the device's location in SharedPreferences
        mSPEditor.putString(Constants.CURRENT_LOCATION, locData).apply()

        mUtility.hideProgressDialog()  // Hide the loading message

        // Display the Toast message
        mUtility.toastMessage(
            requireContext(), getString(R.string.msg_device_location_retrieved)
        )

        /* Update the location spinner to add address information and refresh
         * current location data.
         */
        setLocationSpinner()

        // Stop location updates to conserve device battery and performance
        mFLPC.removeLocationUpdates(mLocCallback)
    }  // end of storeLocationResult method

    // Function to check if location services in the user's device is enabled
    private fun isLocationServicesEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        /* Check if either GPS (Fine Location) or Network (Coarse Location)
         * Provider is enabled
         */
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }  // end of isLocationServicesEnabled method

    // Function to set the spinner items for location options
    private fun setLocationSpinner() {
        // Variable to store location options spinner items
        val locationOptions = resources
            .getStringArray(R.array.location_spinner_options)

        with(binding) {
            // Get the current location data from Shared Preferences
            val curLocJson = mSharedPrefs.getString(Constants.CURRENT_LOCATION, "")

            // Change the location options spinner if the String JSON is not null or empty
            if (!curLocJson.isNullOrEmpty()) {
                // Convert String JSON to CurrentLocation object
                val curLoc = Gson().fromJson(
                    curLocJson, CurrentLocation::class.java
                )

                // Change the text of location options spinner
                actvCurrentLocation.setText(
                    // Make sure curLoc is not null to prevent NPE
                    if (curLoc != null) {
                        // If the code is valid and the address is specified
                        if (curLoc.code in locationOptions.indices
                            && curLoc.fullAddress.isNotEmpty()
                        ) {
                            getString(
                                R.string.selected_location_option,
                                locationOptions[curLoc.code],
                                curLoc.fullAddress
                            )
                        }
                        // If the code is valid only
                        else if (curLoc.code in locationOptions.indices) {
                            locationOptions[curLoc.code]
                        }
                        // Default blank text
                        else {
                            ""
                        }
                    }
                    // Default blank text if curLoc is null
                    else {
                        ""
                    }
                )
            } else {
                actvCurrentLocation.setText("")  // Set the text blank
            }  // end of if-else

            // Prepare the drop down values for location options
            actvCurrentLocation.setAdapter(
                ArrayAdapter(
                    requireContext(), R.layout.spinner_item, locationOptions
                )
            )

            // Actions when the item from location options is selected
            actvCurrentLocation.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    // Device's Location
                    Constants.FROM_DEVICE_LOCATION_CODE -> {
                        // Launch access permission for fine (precise) location
                        activityResultLauncher.launch(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }

                    // From Address
                    Constants.FROM_USER_ADDRESS_CODE -> {
                        // Create an intent to launch AddressesActivity
                        Intent(requireContext(), AddressesActivity::class.java).apply {
                            /* To enable address selection functionality in the
                             * target activity
                             */
                            putExtra(Constants.SELECTABLE_ENABLED, true)
                            // Store the address selection mode code (Current Location)
                            putExtra(
                                Constants.SELECTION_MODE, Constants.SELECT_CURRENT_LOCATION
                            )

                            startActivity(this)  // Opens the activity
                        }
                    }

                }  // end of when
            }  // end of setOnItemClickListener

        }  // end of with(binding)

        setCategoryAdapter()  // Set the category panels in the home page
    }  // end of setLocationSpinner method

    // Function to set the RecyclerView adapter of Browse and Categories
    private fun setCategoryAdapter() {
        with(binding) {
            // Create an object of Market Browse Adapter
            browseAdapter = MarketBrowseAdapter(
                requireActivity() as MainActivity,
                resources.getStringArray(R.array.browse_market_categories),
                resources.getStringArray(R.array.browse_market_description),
                !mSharedPrefs.getString(Constants.CURRENT_LOCATION, "").isNullOrEmpty()
            )
            // Create an object of Market Categories Adapter
            categoriesAdapter = MarketCategoriesAdapter(
                requireActivity() as MainActivity,
                resources.getStringArray(R.array.market_categories_list),
                !mSharedPrefs.getString(Constants.CURRENT_LOCATION, "").isNullOrEmpty()
            )

            // Sets the adapter of Market Browse RecyclerView
            rvMarketBrowse.adapter = browseAdapter
            // Sets the adapter of Market Categories RecyclerView
            rvMarketCategories.adapter = categoriesAdapter

            // Make the NestedScrollView visible
            if (!nsvHomeView.isVisible) nsvHomeView.visibility = View.VISIBLE
        }  // end of with(binding)

    }  // end of setCategoryAdapter method

}  // end of HomeFragment class
