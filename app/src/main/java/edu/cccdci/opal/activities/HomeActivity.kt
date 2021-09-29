package edu.cccdci.opal.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityHomeBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader

class HomeActivity : TemplateActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mUserInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController

            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.fragment_home,
                    R.id.fragment_markets,
                    R.id.fragment_categories,
                    R.id.fragment_notifications
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            bottomNavigationView.setupWithNavController(navController)

            //Adds the Hamburger Button Toggle in Action Bar
            toggle = ActionBarDrawerToggle(
                this@HomeActivity, dlHomeDrawer,
                R.string.open, R.string.close
            )

            dlHomeDrawer.addDrawerListener(toggle)
            toggle.syncState()

            //Sets up Navigation Action Bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            nvSidebar.setNavigationItemSelectedListener(this@HomeActivity)
        }

    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Go to User Profile Settings when this ImageView is clicked
                R.id.iv_nav_profile_pic -> {
                    //Create an Intent to launch UserProfileActivity
                    val intent = Intent(
                        this@HomeActivity,
                        UserProfileActivity::class.java
                    )
                    //Add extra user information to intent
                    intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                    //Opens the edit user profile
                    startActivity(intent)
                    //Closes the drawer layout
                    binding.dlHomeDrawer.closeDrawer(GravityCompat.START)
                }

            } //end of when

        } //end of if

    } //end of onClick method

    //Operations to do when this activity is active again
    override fun onResume() {
        super.onResume()

        //Gets the user profile data
        FirestoreClass().getUserDetails(this@HomeActivity)
    } //end of onResume method

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Send to Address List
            R.id.nav_address -> shortToastMessage(
                this@HomeActivity,
                "You clicked Address"
            ).show()

            //TODO: Send to User Order History
            R.id.nav_order_history -> shortToastMessage(
                this@HomeActivity,
                "You clicked Order History"
            ).show()

            //TODO: Send to Application Settings
            R.id.nav_settings -> shortToastMessage(
                this@HomeActivity,
                "You clicked Settings"
            ).show()

            //Signs the user out
            R.id.nav_signout -> signOutUser()

            //TODO: Send to About Us
            R.id.nav_about_us -> shortToastMessage(
                this@HomeActivity,
                "You clicked About Us"
            ).show()

            //TODO: Send to Join Community
            R.id.nav_join -> shortToastMessage(
                this@HomeActivity,
                "You clicked Join Community"
            ).show()
        } //end of when

        return true
    } //end of onNavigationItemSelected method

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (toggle.onOptionsItemSelected(item))
            super.onOptionsItemSelected(item)
        else
            true

    //Function to set up user information in the sidebar header
    fun setSideNavProfileHeader(sp: SharedPreferences, user: User) {

        mUserInfo = user

        //Get user information from Shared Preferences
        val signedInName = sp.getString(
            Constants.SIGNED_IN_FULL_NAME,
            "undefined undefined")!!
        val signedInUserName = sp.getString(
            Constants.SIGNED_IN_USERNAME,
            "undefined")!!
        val signedInProfilePic = sp.getString(
            Constants.SIGNED_IN_PROFILE_PIC,
            "")!!

        //Gets the header view from sidebar
        val header = binding.nvSidebar.getHeaderView(0)

        //Get ids from sidebar header
        val navFullName: TextView = header.findViewById(R.id.tv_profile_name)!!
        val navUserName: TextView = header.findViewById(R.id.tv_profile_username)!!
        val navProfile: ImageView = header.findViewById(R.id.iv_nav_profile_pic)!!

        //Set the current logged in information in the sidebar header
        navFullName.text = signedInName
        navUserName.text = signedInUserName

        //Change the profile picture with the image in the Cloud Storage
        GlideLoader(this@HomeActivity)
            .loadUserPicture(signedInProfilePic, navProfile)

        //Click event for Navigation Profile Picture ImageView
        navProfile.setOnClickListener(this@HomeActivity)

    } //end of setSideNavProfileHeader method

    //Function to sign out the user
    private fun signOutUser() {
        //Sign out the current Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        //Create an Intent to launch LoginActivity
        val intent = Intent(
            this@HomeActivity,
            LoginActivity::class.java
        )
        //To ensure that no more activity layers are active after the user signs out
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        shortToastMessage(
            this@HomeActivity, resources.getString(R.string.msg_signed_out)
        ).show()

        startActivity(intent) //Opens the login page
        finish() //Closes the current activity

    } //end of signOutUser method

}
