package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMainBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class MainActivity : UtilityClass(),
    NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserInfo: User
    private lateinit var mNavController: NavController
    private lateinit var mAppBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // Prepare NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            mNavController = navHostFragment.navController

            // Setup Bottom Navigation
            bottomNavView.setupWithNavController(mNavController)

            // Prepare Drawer Navigation (Sidebar)
            mAppBarConfiguration = AppBarConfiguration(
                mNavController.graph, dlHomeDrawer
            )

            // Setup Drawer Navigation
            NavigationUI.setupActionBarWithNavController(
                this@MainActivity, mNavController, dlHomeDrawer
            )
            // Setup Sidebar
            NavigationUI.setupWithNavController(nvSidebar, mNavController)

            // Add functionality to the sidebar
            nvSidebar.setNavigationItemSelectedListener(this@MainActivity)

            // Toggle bottom navigation visibility
            mNavController.addOnDestinationChangedListener { _, destination, _ ->
                /* Bottom nav is visible for home, markets, categories,
                 * and notifications fragments
                 */
                when (destination.id) {
                    R.id.fragment_home,
                    R.id.fragment_markets,
                    R.id.fragment_categories,
                    R.id.fragment_notifs -> bottomNavView.visibility = View.VISIBLE

                    else -> binding.bottomNavView.visibility = View.GONE
                }
            }  // end of addOnDestinationChangedListener
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is active again
    override fun onResume() {
        super.onResume()

        // Gets the user profile data
        FirestoreClass().getUserDetails(this@MainActivity)
    }  // end of onResume method

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
                    // Create an Intent to launch UserProfileActivity
                    val intent = Intent(
                        this@MainActivity,
                        UserProfileActivity::class.java
                    )
                    // Add extra user information to intent
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                    // Opens the edit user profile
                    startActivity(intent)
                    // Closes the drawer layout
                    binding.dlHomeDrawer.closeDrawer(GravityCompat.START)
                }
            }  // end of when
        }  // end of if

    }  // end of onClick method

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
    }  // end of onSupportNavigateUp method

    // onNavigationItemSelected events are declared here
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Sends user to Addresses List
            R.id.nav_addresses -> navigateFragment(R.id.home_to_addresses)

            // Sends user to User Order History
            R.id.nav_order_history -> navigateFragment(R.id.home_to_order_history)

            // Sends user to Application Settings
            R.id.nav_settings -> navigateFragment(R.id.home_to_settings)

            // Logs the user out
            R.id.nav_log_out -> showAlertDialog(
                this@MainActivity,
                resources.getString(R.string.dialog_log_out_title),
                resources.getString(R.string.dialog_log_out_message)
            )

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
            R.id.nav_your_market -> navigateFragment(R.id.home_to_your_market)

            // Sends user to Product Inventory
            R.id.nav_products -> startActivity(
                Intent(this@MainActivity, ProductActivity::class.java)
            )

            // Sends user to Customer Transaction Details
            R.id.nav_customer_transactions -> navigateFragment(R.id.home_to_cust_trans)

            // Sends user to Sales Insights Page
            R.id.nav_insights -> navigateFragment(R.id.home_to_insights)
        }  // end of when

        // Closes the drawer layout
        binding.dlHomeDrawer.closeDrawer(GravityCompat.START)

        return true
    }  // end of onNavigationItemSelected method

    // Function to navigate from source fragment to destination fragment
    private fun navigateFragment(resId: Int) {
        mNavController.navigate(resId)
    }  // end of navigateFragment method

    // Function to set up user information in the sidebar header
    fun setSideNavProfileHeader(sp: SharedPreferences, user: User) {
        mUserInfo = user  // To be used for Parcelable

        // Get user information from Shared Preferences
        val signedInName = sp.getString(
            Constants.SIGNED_IN_FULL_NAME, "undefined undefined"
        )!!
        val signedInUserName = sp.getString(
            Constants.SIGNED_IN_USERNAME, "undefined"
        )!!
        val signedInProfilePic = sp.getString(
            Constants.SIGNED_IN_PROFILE_PIC, ""
        )!!
        val signedInUserRole = sp.getBoolean(
            Constants.SIGNED_IN_USER_ROLE, false
        )

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
        navUserRole.text = if (signedInUserRole)
            resources.getString(R.string.role_vendor)
        else
            resources.getString(R.string.role_customer)

        // Change the profile picture with the image in the Cloud Storage
        GlideLoader(this@MainActivity)
            .loadPicture(signedInProfilePic, navProfile)

        // Hide some sidebar menu items depending on user's role
        hideBasedOnUserRole(binding.nvSidebar.menu, signedInUserRole)

        // Click event for Navigation Profile Picture ImageView
        navProfile.setOnClickListener(this@MainActivity)

    }  // end of setSideNavProfileHeader method

    // Function to hide sidebar menu items based on user role
    private fun hideBasedOnUserRole(menu: Menu, role: Boolean) {
        if (role) {
            // Hide only Become a Vendor
            menu.findItem(R.id.nav_become_vendor)!!.isVisible = false
            menu.findItem(R.id.nav_vendor_only)!!.isVisible = true
        } else {
            // Hide Vendor Center menu group
            menu.findItem(R.id.nav_vendor_only)!!.isVisible = false
            menu.findItem(R.id.nav_become_vendor)!!.isVisible = true
        }
    }  // end of hideBasedOnUserRole method

    // Function to sign out the user
    fun signOutUser() {
        // Sign out the current Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Create an Intent to launch LoginActivity
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        // To ensure that no more activity layers are active after the user signs out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Display a Toast message
        toastMessage(
            this@MainActivity,
            resources.getString(R.string.msg_logged_out)
        )

        startActivity(intent)  // Opens the log in activity
        finish()  // Closes the current activity

    }  // end of signOutUser method

}  // end of MainActivity class
