package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMainBinding
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class MainActivity : UtilityClass(),
    NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener,
    View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private lateinit var mNavGraph: NavGraph
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private var mUserInfo: User? = null
    private var mCurLocJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        with(binding) {
            setContentView(root)

            // Prepare NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            mNavController = navHostFragment.navController

            // Get the navigation graph of bottom and drawer navigation
            mNavGraph = mNavController.navInflater
                .inflate(R.navigation.mobile_navigation)
            // Set the graph of navigation controller
            mNavController.graph = mNavGraph

            // Setup Bottom Navigation
            bottomNavView.setupWithNavController(mNavController)

            // Prepare Drawer Navigation (Sidebar)
            mAppBarConfiguration = AppBarConfiguration(mNavController.graph, dlHomeDrawer)

            // Setup Drawer Navigation (Sidebar)
            NavigationUI.setupActionBarWithNavController(
                this@MainActivity, mNavController, dlHomeDrawer
            )
            NavigationUI.setupWithNavController(nvSidebar, mNavController)

            // Add functionality to the sidebar
            nvSidebar.setNavigationItemSelectedListener(this@MainActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible
    override fun onStart() {
        super.onStart()

        // Display the loading message
        showProgressDialog(
            this@MainActivity, this@MainActivity,
            getString(R.string.msg_please_wait)
        )

        // Gets the user profile data
        FirestoreClass().getUserDetails(this@MainActivity)
    }  // end of onStart method

    // Override the function to make the user double press back to exit
    override fun onBackPressed() {
        // Unless the current fragment is home, don't double back to exit
        if (mNavController.currentDestination?.id == R.id.fragment_home)
            doubleBackToExit()
        else
            super.onBackPressed()
    }  // end of onBackPressed method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Go to User Profile Settings when this ImageView is clicked
                R.id.iv_nav_profile_pic -> {
                    // Closes the drawer layout
                    binding.dlHomeDrawer.closeDrawer(GravityCompat.START)

                    // Create an Intent to launch UserProfileActivity
                    Intent(
                        this@MainActivity, UserProfileActivity::class.java
                    ).apply {
                        // Add extra user information to intent
                        putExtra(Constants.EXTRA_USER_INFO, mUserInfo!!)

                        startActivity(this)  // Opens the edit user profile
                    }  // end of apply
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Overriding function to create functionality for Up button (from Top Action Bar)
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
    }  // end of onSupportNavigateUp method

    // onNavigationItemSelected events are declared here
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Sends user to Addresses List
            R.id.nav_addresses -> startActivity(
                Intent(this@MainActivity, AddressesActivity::class.java)
            )

            // Sends user to User Order History
            R.id.nav_order_history -> navigateFragment(R.id.home_to_order_history)

            // Sends user to Application Settings
            R.id.nav_settings -> navigateFragment(R.id.home_to_settings)

            // Logs the user out
            R.id.nav_log_out -> {
                // Display an alert dialog with two action buttons (Log Out & Cancel)
                DialogClass(this@MainActivity).alertDialog(
                    getString(R.string.dialog_log_out_title),
                    getString(R.string.dialog_log_out_message),
                    getString(R.string.dialog_btn_log_out),
                    getString(R.string.dialog_btn_cancel)
                )
            }

            // Sends user to About Us Page
            R.id.nav_about_us -> navigateFragment(R.id.home_to_about)

            // Sends user to Feedback Page
            R.id.nav_feedback -> navigateFragment(R.id.home_to_feedback)

            // Sends user to Privacy Policy Page
            R.id.nav_privacy -> navigateFragment(R.id.home_to_privacy)

            // Sends user (customer) to Become a Vendor
            R.id.nav_become_vendor -> startActivity(
                Intent(this@MainActivity, BecomeVendorActivity::class.java)
            )

            // Send to User's Market
            R.id.nav_my_market -> Intent(
                this@MainActivity, MyMarketActivity::class.java
            ).apply {
                // Add market ID data to intent
                putExtra(Constants.MARKET_ID_DATA, mUserInfo?.marketID)

                startActivity(this)  // Opens the vendor's market profile
            }

            // Sends user to Product Inventory
            R.id.nav_products -> Intent(
                this@MainActivity, ProductActivity::class.java
            ).apply {
                // Add market ID data to intent
                putExtra(Constants.MARKET_ID_DATA, mUserInfo?.marketID)

                startActivity(this)  // Opens the product inventory
            }

            // Sends user to Sales History
            R.id.nav_sales_history -> navigateFragment(R.id.home_to_sales_history)

            // Temporary item
            R.id.nav_insights -> startActivity(
                Intent(this@MainActivity, InsightsActivity::class.java)
            )

        }  // end of when

        // Closes the drawer layout
        binding.dlHomeDrawer.closeDrawer(GravityCompat.START)

        return true
    }  // end of onNavigationItemSelected method

    // Overriding function to trigger events when the fragment was changed
    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        // Bottom navigation is visible for home, categories, notifications fragments
        binding.bottomNavView.visibility = when (destination.id) {
            R.id.fragment_home,
            R.id.fragment_cart,
            R.id.fragment_notifs -> View.VISIBLE

            else -> View.GONE
        }

        when (destination.id) {
            R.id.fragment_home -> {
                if (mUserInfo != null) {
                    NavArgument.Builder().setDefaultValue(mUserInfo!!).build()
                        .also { destination.addArgument(Constants.EXTRA_USER_INFO, it) }
                }
            }
        }  // end of when

    }  // end of onDestinationChanged method

    // Function to navigate from source fragment to destination fragment
    private fun navigateFragment(resId: Int) {
        mNavController.navigate(resId)
    }  // end of navigateFragment method

    // Function to set up user information in the sidebar header
    internal fun setNavigationAttributes(user: User) {
        // Get user information from Shared Preferences
        val signedInName = mSharedPrefs.getString(
            Constants.SIGNED_IN_FULL_NAME, "undefined undefined"
        )!!
        val signedInUserName = mSharedPrefs.getString(
            Constants.SIGNED_IN_USERNAME, "undefined"
        )!!
        val signedInProfilePic = mSharedPrefs.getString(
            Constants.SIGNED_IN_PROFILE_PIC, ""
        )!!
        val signedInUserRole = mSharedPrefs.getBoolean(
            Constants.SIGNED_IN_USER_ROLE, false
        )

        // Get the current location in String JSON format
        mCurLocJson = mSharedPrefs.getString(Constants.CURRENT_LOCATION, "")

        // Gets the header view from sidebar
        val header = binding.nvSidebar.getHeaderView(0)

        // Get ids from sidebar header
        val navFullName: TextView = header.findViewById(R.id.tv_profile_name)!!
        val navUserName: TextView = header.findViewById(R.id.tv_profile_username)!!
        val navUserRole: TextView = header.findViewById(R.id.tv_role_indicator)!!
        val navProfile: ImageView = header.findViewById(R.id.iv_nav_profile_pic)!!

        // Set the current logged in information in the sidebar header
        navFullName.text = signedInName
        navUserName.text = signedInUserName

        // Set the role indicator of the user
        navUserRole.text = getString(
            if (signedInUserRole) R.string.role_vendor
            else R.string.role_customer
        )

        // Change the profile picture with the image in the Cloud Storage
        GlideLoader(this@MainActivity)
            .loadImage(signedInProfilePic, navProfile)

        // Hide some sidebar menu items depending on user's role
        hideBasedOnUserRole(binding.nvSidebar.menu, signedInUserRole)

        /* In case the String JSON for user's current location is null or empty,
         * fill up the Shared Preference of the said location if it is available
         * in the Firestore.
         */
        if (mCurLocJson.isNullOrEmpty()) {
            if (user.locSettings != null) {
                mSPEditor.putString(
                    Constants.CURRENT_LOCATION, Gson().toJson(user.locSettings)
                ).apply()
            }
        }

        // Modify the mUserInfo if there are any changes detected
        if (mUserInfo != user) {
            // Create an argument to store user data for Home Fragment
            val navArg = NavArgument.Builder().setDefaultValue(user).build()

            // Adds the parcelable argument for Home Fragment
            mNavGraph.addArgument(Constants.EXTRA_USER_INFO, navArg)
            mNavController.graph = mNavGraph

            mUserInfo = user  // To be used for Parcelable
        }

        // Click event for Navigation Profile Picture ImageView
        navProfile.setOnClickListener(this@MainActivity)
        // Toggle bottom navigation visibility
        mNavController.addOnDestinationChangedListener(this@MainActivity)

        hideProgressDialog()  // Hide the loading message
    }  // end of setNavigationAttributes method

    // Function to hide sidebar menu items based on user role
    private fun hideBasedOnUserRole(menu: Menu, isVendor: Boolean) {
        if (isVendor) {
            // Hide only Become a Vendor
            menu.findItem(R.id.nav_become_vendor)!!.isVisible = false
            menu.findItem(R.id.nav_vendor_only)!!.isVisible = true
        } else {
            // Hide Vendor Center menu group
            menu.findItem(R.id.nav_vendor_only)!!.isVisible = false
            menu.findItem(R.id.nav_become_vendor)!!.isVisible = true
        }
    }  // end of hideBasedOnUserRole method

    // Function to log out the user (and save the current location if available)
    internal fun logOutUser() {
        // Get the most recent location data from Shared Preference
        mCurLocJson = mSharedPrefs.getString(Constants.CURRENT_LOCATION, "")!!

        // Save the most recent location data if the String JSON is not empty
        if (!mCurLocJson.isNullOrEmpty()) {
            // Display the loading message
            showProgressDialog(
                this@MainActivity, this@MainActivity,
                getString(R.string.msg_please_wait)
            )

            // Update the user's location settings in Firestore
            FirestoreClass().updateUserProfileData(
                this@MainActivity, hashMapOf(
                    Constants.LOCATION_SETTINGS to Gson().fromJson(
                        mCurLocJson, CurrentLocation::class.java
                    )
                )
            )
        } else {
            userAuthLogOut()  // Just log out the user
        }
    }  // end of logOutUser method

    // Function to log out the user's session in Firebase Authentication
    internal fun userAuthLogOut() {
        // Clear the current location Shared Preference data if it is not null or empty
        if (!mCurLocJson.isNullOrEmpty()) {
            hideProgressDialog()

            mSPEditor.putString(Constants.CURRENT_LOCATION, "").apply()
        }

        // Log out the current Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Create an Intent to launch LoginActivity
        Intent(this@MainActivity, LoginActivity::class.java).apply {
            // To ensure that no more activity layers are active after the user signs out
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Display a Toast message
            toastMessage(
                this@MainActivity, getString(R.string.msg_logged_out)
            )

            startActivity(this)  // Opens the log in activity
            finish()  // Closes the current activity
        }  // end of apply

    }  // end of userAuthLogOut method

}  // end of MainActivity class
