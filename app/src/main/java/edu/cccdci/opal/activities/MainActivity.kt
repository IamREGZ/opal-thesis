package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMainBinding

class MainActivity : TemplateActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        //Force disables dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            //Adds the Hamburger Button Toggle in Action Bar
            toggle = ActionBarDrawerToggle(
                this@MainActivity, dlHomepage,
                R.string.open, R.string.close
            )

            dlHomepage.addDrawerListener(toggle)
            toggle.syncState()

            //Sets up Navigation Action Bar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            nvSidebar.setNavigationItemSelectedListener {

                when (it.itemId) {
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

                    //Logouts the User
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

                true
            } //end of setNavigationItemSelectedListener

        } //end of with(binding)

    } //end of onCreate method

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (toggle.onOptionsItemSelected(item))
            super.onOptionsItemSelected(item)
        else
            true

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
