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

class MainActivity : TemplateActivity(),
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

            //Prepare NavHostFragment
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            mNavController = navHostFragment.navController

            //Setup Bottom Navigation
            bottomNavView.setupWithNavController(mNavController)

            //Prepare Drawer Navigation (Sidebar)
            mAppBarConfiguration = AppBarConfiguration(
                mNavController.graph, dlHomeDrawer
            )

            //Setup Drawer Navigation
            NavigationUI.setupActionBarWithNavController(
                this@MainActivity, mNavController, dlHomeDrawer
            )
            //Setup Sidebar
            NavigationUI.setupWithNavController(nvSidebar, mNavController)

            //Add functionality to the sidebar
            nvSidebar.setNavigationItemSelectedListener(this@MainActivity)
        } //end of with(binding)

    } //end of onCreate method

    //Operations to do when this activity is active again
    override fun onResume() {
        super.onResume()

        //Gets the user profile data
        FirestoreClass().getUserDetails(this@MainActivity)
    } //end of onResume method

    //Override the function to make the user double press back to exit
    override fun onBackPressed() {
        //Unless the current fragment is home, don't double back to exit
        if (mNavController.currentDestination?.id == R.id.fragment_home)
            doubleBackToExit()
        else
            super.onBackPressed()
    } //end of onBackPressed method

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Go to User Profile Settings when this ImageView is clicked
                R.id.iv_nav_profile_pic -> {
                    //Create an Intent to launch UserProfileActivity
                    val intent = Intent(
                        this@MainActivity,
                        UserProfileActivity::class.java
                    )
                    //Add extra user information to intent
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                    //Opens the edit user profile
                    startActivity(intent)
                    //Closes the drawer layout
                    binding.dlHomeDrawer.closeDrawer(GravityCompat.START)
                }

                //TODO: Send to Item Cart
                R.id.tab_cart -> toastMessage(
                    this@MainActivity, "You clicked Item Cart"
                ).show()

                //TODO: Send to Messages
                R.id.tab_messages -> toastMessage(
                    this@MainActivity, "You clicked Messages"
                ).show()

            } //end of when

        } //end of if

    } //end of onClick method

    //Override the function to add two icons on top app bar (cart and messages)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    } //end of onCreateOptionsMenu method

    override fun onSupportNavigateUp(): Boolean =
        NavigationUI.navigateUp(mNavController, mAppBarConfiguration)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Send to Address List
            R.id.nav_address -> toastMessage(
                this@MainActivity, "You clicked Address"
            ).show()

            //TODO: Send to User Order History
            R.id.nav_order_history -> toastMessage(
                this@MainActivity, "You clicked Order History"
            ).show()

            //TODO: Send to Application Settings
            R.id.nav_settings -> toastMessage(
                this@MainActivity, "You clicked Settings"
            ).show()

            //Signs the user out
            R.id.nav_sign_out -> signOutUser()

            //TODO: Send to About Us
            R.id.nav_about_us -> toastMessage(
                this@MainActivity, "You clicked About Us"
            ).show()

            //TODO: Send to Feedback Page
            R.id.nav_feedback -> toastMessage(
                this@MainActivity, "You clicked Feedback"
            ).show()

            //TODO: Send to Privacy Policy Page
            R.id.nav_privacy -> toastMessage(
                this@MainActivity, "You clicked Privacy Policy"
            ).show()

            //TODO: Send to Become a Vendor (Customers)
            R.id.nav_become_vendor -> toastMessage(
                this@MainActivity, "You clicked Become a Vendor"
            ).show()

            //TODO: Send to User's Market
            R.id.nav_your_market -> toastMessage(
                this@MainActivity, "You clicked Your Market"
            ).show()

            //TODO: Send to Product Inventory
            R.id.nav_products -> toastMessage(
                this@MainActivity, "You clicked Products"
            ).show()

            //TODO: Send to Orders List
            R.id.nav_orders -> toastMessage(
                this@MainActivity, "You clicked Orders"
            ).show()

            //TODO: Send to Sales Insights
            R.id.nav_insights -> toastMessage(
                this@MainActivity, "You clicked Insights"
            ).show()
        } //end of when

        return true
    } //end of onNavigationItemSelected method

    //Function to set up user information in the sidebar header
    fun setSideNavProfileHeader(sp: SharedPreferences, user: User) {
        //To be used for Parcelable
        mUserInfo = user

        //Get user information from Shared Preferences
        val signedInName = sp.getString(
            Constants.SIGNED_IN_FULL_NAME,
            "undefined undefined"
        )!!
        val signedInUserName = sp.getString(
            Constants.SIGNED_IN_USERNAME,
            "undefined"
        )!!
        val signedInProfilePic = sp.getString(
            Constants.SIGNED_IN_PROFILE_PIC,
            ""
        )!!
        val signedInUserRole = sp.getBoolean(
            Constants.SIGNED_IN_USER_ROLE,
            false
        )

        //Gets the header view from sidebar
        val header = binding.nvSidebar.getHeaderView(0)

        //Get ids from sidebar header
        val navFullName: TextView = header.findViewById(R.id.tv_profile_name)!!
        val navUserName: TextView = header.findViewById(R.id.tv_profile_username)!!
        val navUserRole: TextView = header.findViewById(R.id.tv_role_indicator)!!
        val navProfile: ImageView = header.findViewById(R.id.iv_nav_profile_pic)!!

        //Declare variables for getting menu items in sidebar
        val menu = binding.nvSidebar.menu
        val itemToHide: MenuItem

        //Set the current logged in information in the sidebar header
        navFullName.text = signedInName
        navUserName.text = signedInUserName

        //Set the role indicator of the user, as well as the available menus
        navUserRole.text = if (signedInUserRole) {
            //If the user is a vendor, hide the menu item "Become a Vendor"
            itemToHide = menu.findItem(R.id.nav_become_vendor)!!
            resources.getString(R.string.role_vendor) //Displays Vendor
        } else {
            //If the user is a customer, hide the menu items for vendors only
            itemToHide = menu.findItem(R.id.nav_vendor_only)!!
            resources.getString(R.string.role_customer) //Displays Customer
        }
        itemToHide.isVisible = false //Hide the given menu items

        //Change the profile picture with the image in the Cloud Storage
        GlideLoader(this@MainActivity)
            .loadUserPicture(signedInProfilePic, navProfile)

        //Click event for Navigation Profile Picture ImageView
        navProfile.setOnClickListener(this@MainActivity)

    } //end of setSideNavProfileHeader method

    //Function to sign out the user
    private fun signOutUser() {
        //Sign out the current Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        //Create an Intent to launch LoginActivity
        val intent = Intent(
            this@MainActivity,
            LoginActivity::class.java
        )
        //To ensure that no more activity layers are active after the user signs out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        toastMessage(
            this@MainActivity, resources.getString(R.string.msg_signed_out)
        ).show()

        startActivity(intent) //Opens the login page
        finish() //Closes the current activity

    } //end of signOutUser method

} //end of MainActivity class
