package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
                //Opens the Login Activity
                startActivity(
                    Intent(
                        this@SplashScreenActivity,
                        LoginActivity::class.java
                    )
                )
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish() //Closes the Splash Screen
            } //end of withEndAction

        } //end of with(binding)

//        //Non-animated Splash Screen
//        @Suppress("DEPRECATION")
//        Handler().postDelayed(
//            {
//                //Launch the Main Activity
//                startActivity(
//                    Intent(this@SplashScreenActivity, LoginActivity::class.java)
//                )
//                finish() //Closes the activity
//            },
//            1500 //Delay Duration
//        )
    } //end of onCreate method

} //end of SplashScreenActivity class