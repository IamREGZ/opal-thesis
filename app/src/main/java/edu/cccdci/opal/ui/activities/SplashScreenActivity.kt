package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //Force disables dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        //Animated Splash Screen
        with(binding) {
            setContentView(root)

            //Makes the activity full screen
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }

            ivLogo.alpha = 0f //Initial alpha for logo

            //Animate the splash screen logo by fading in
            ivLogo.animate().setDuration(1500).alpha(1f).withEndAction {
                //Opens either Log In or Home Activity
                startActivity(
                    Intent(
                        this@SplashScreenActivity,
                        checkCurrentUserSignIn() //Check for signed in user
                    )
                )

                //Change the default transition
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )

                finish() //Closes the Splash Screen
            } //end of withEndAction

        } //end of with(binding)

    } //end of onCreate method

    //Function to check if there's a user signed in in the application
    private fun checkCurrentUserSignIn(): Class<*> {
        //Get the current user signed in from Firebase Authentication
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        //If there's a user signed in, go to the home page
        return if (firebaseUser != null)
            HomeActivity::class.java
        //If none, go to log in page instead
        else
            LoginActivity::class.java
    } //end of checkCurrentUserSignIn method

} //end of SplashScreenActivity class