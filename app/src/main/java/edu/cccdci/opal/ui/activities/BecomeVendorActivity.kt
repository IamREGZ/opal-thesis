package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityBecomeVendorBinding
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class BecomeVendorActivity : UtilityClass() {

    private lateinit var binding: ActivityBecomeVendorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityBecomeVendorBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbBecomeVendorActivity, false)

            btnApplyNow.setOnClickListener {
                // If password is not empty,
                if (FormValidation(this@BecomeVendorActivity)
                        .validateAuthPassword(etVendorPass)
                ) {
                    // Proceed with account verification
                    verifyCredentials(etVendorPass.text.toString().trim { it <= ' ' })
                }
            }  // end of setOnClickListener
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible again
    override fun onRestart() {
        super.onRestart()

        // Clear the password field for security purposes
        binding.etVendorPass.text!!.clear()
    }  // end of onRestart method

    // Function to verify credentials, and then upgrade user to vendor
    private fun verifyCredentials(password: String) {
        // Display the loading message
        showProgressDialog(
            this@BecomeVendorActivity, this@BecomeVendorActivity,
            resources.getString(R.string.msg_please_wait)
        )

        // Get credentials of the current user
        val credential = EmailAuthProvider.getCredential(
            FirebaseAuth.getInstance().currentUser!!.email!!, password
        )

        // Verify credentials
        FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
            .addOnCompleteListener { task ->
                hideProgressDialog()  // Hide the loading message

                // Correct credentials
                if (task.isSuccessful) {
                    // Open the Vendor Registration Activity
                    startActivity(
                        Intent(
                            this@BecomeVendorActivity,
                            MarketEditorActivity::class.java
                        )
                    )
                    finish()  // Closes the activity
                }
                // Wrong credentials
                else {
                    // Clear the password field for security purposes
                    binding.etVendorPass.text!!.clear()

                    // Display the error message
                    showSnackBar(
                        this@BecomeVendorActivity,
                        task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of reauthenticate

    }  // end of verifyCredentials method

}  // end of BecomeVendorActivity class
