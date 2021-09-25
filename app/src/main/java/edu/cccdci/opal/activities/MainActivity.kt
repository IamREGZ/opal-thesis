package edu.cccdci.opal.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMainBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.utils.Constants

class MainActivity :
    TemplateActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            //Sets up the Sidebar Profile Header
            setSideNavProfileHeader(
                getSharedPreferences(Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE),
                nvSidebar.getHeaderView(0)
            )

            //Adds the Hamburger Button Toggle in Action Bar
            toggle = ActionBarDrawerToggle(
                this@MainActivity, dlHomepage,
                R.string.open, R.string.close
            )

            dlHomepage.addDrawerListener(toggle)
            toggle.syncState()

            //Sets up Navigation Action Bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            //Item Selected event for any sidebar items
            nvSidebar.setNavigationItemSelectedListener(this@MainActivity)
        } //end of with(binding)

    } //end of onCreate method

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Send to Home Page
            R.id.nav_home -> shortToastMessage(
                this@MainActivity,
                "You clicked Home"
            ).show()

            //TODO: Send to Address List
            R.id.nav_address -> shortToastMessage(
                this@MainActivity,
                "You clicked Address"
            ).show()

            //TODO: Send to Stores Near User
            R.id.nav_near_me -> shortToastMessage(
                this@MainActivity,
                "You clicked Near Me"
            ).show()

            //TODO: Send to User Order History
            R.id.nav_order_history -> shortToastMessage(
                this@MainActivity,
                "You clicked Order History"
            ).show()

            //TODO: Send to Application Settings
            R.id.nav_settings -> shortToastMessage(
                this@MainActivity,
                "You clicked Settings"
            ).show()

            //Signs the user out
            R.id.nav_signout -> signOutUser()

            //TODO: Send to About Us
            R.id.nav_about_us -> shortToastMessage(
                this@MainActivity,
                "You clicked About Us"
            ).show()

            //TODO: Send to Join Community
            R.id.nav_join -> shortToastMessage(
                this@MainActivity,
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

    //Function to set up user information in the Sidebar Header
    private fun setSideNavProfileHeader(sp: SharedPreferences, head: View) {
        //Get user information from Shared Preferences
        val signedInName = sp.getString(
            Constants.SIGNED_IN_FULL_NAME, "undefined undefined"
        )!!
        val signedInUserName = sp.getString(
            Constants.SIGNED_IN_USERNAME, "undefined"
        )!!

        //Get ids from sidebar header
        val navFullName: TextView? = head.findViewById(R.id.tv_profile_name)
        val navUserName: TextView? = head.findViewById(R.id.tv_profile_username)
        val navProfile: CircleImageView? = head.findViewById(R.id.iv_nav_profile_pic)

        //Set the current logged in information in the sidebar header
        navFullName?.text = signedInName
        navUserName?.text = signedInUserName

        //Go to User Profile Settings when this ImageView is clicked
        navProfile?.setOnClickListener {
            var userInfo = User()

            //Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                userInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)!!
            }

            //Create an Intent to launch UserProfileActivity
            val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
            //Add extra user information to intent
            intent.putExtra(Constants.EXTRA_USER_INFO, userInfo)

            //Opens the edit user profile
            startActivity(intent)
            //Closes the drawer layout
            binding.dlHomepage.closeDrawer(GravityCompat.START)
        } //end of setOnClickListener

    } //end of setSideNavProfileHeader method

    //Function to sign out the user
    private fun signOutUser() {
        //Sign out the current Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        //Opens the login page
        startActivity(
            Intent(
                this@MainActivity,
                LoginActivity::class.java
            )
        )
        finish() //Closes the current activity

        //Displays a Toast message
        longToastMessage(
            this@MainActivity,
            resources.getString(R.string.msg_logout_success)
        ).show()

    } //end of signOutUser method

} //end of MainActivity class
