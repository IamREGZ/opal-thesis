package edu.cccdci.opal.ui.activities

import android.os.Bundle
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityBecomeVendorBinding
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class BecomeVendorActivity : UtilityClass() {

    private lateinit var binding: ActivityBecomeVendorBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityBecomeVendorBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbBecomeVendorActivity, false)

            btnApplyNow.setOnClickListener {
                // Get the text from password field
                val password: String = etVendorPass.text.toString()
                    .trim { it <= ' ' }

                if (password.isEmpty()) {
                    // Error if password field is empty
                    showSnackBar(
                        this@BecomeVendorActivity,
                        resources.getString(R.string.err_blank_password),
                        true
                    )
                } else {
                    //Proceed with account deletion
                    verifyCredentials(password)
                }
            }  // end of setOnClickListener

        }  // end of with(binding)

    }  // end of onCreate method

    // Function to verify credentials, and then upgrade user to vendor
    private fun verifyCredentials(password: String) {
        // Display the loading message
        showProgressDialog(
            this@BecomeVendorActivity,
            this@BecomeVendorActivity,
            resources.getString(R.string.msg_please_wait)
        )

        // Get credentials of the current user
        val credential = EmailAuthProvider.getCredential(
            FirebaseAuth.getInstance().currentUser!!.email!!, password
        )

        // Verify credentials
        FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
            .addOnCompleteListener { task ->
                // Correct credentials
                if (task.isSuccessful) {
                    // Proceed to update vendor field in the current user document
                    FirestoreClass().updateUserProfileData(
                        this@BecomeVendorActivity,
                        hashMapOf(Constants.VENDOR to true)
                    )
                } else {
                    // Wrong credentials
                    // Hide the loading message
                    hideProgressDialog()

                    // Clear the password field
                    binding.etVendorPass.text!!.clear()

                    // Display the error message
                    showSnackBar(
                        this@BecomeVendorActivity,
                        task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of reauthenticate

    }  // end of validateCredentials method

    // Function to prompt that the user was upgraded to vendor
    fun upgradedToVendorPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Displays the Toast message
        toastMessage(
            this@BecomeVendorActivity,
            resources.getString(R.string.msg_user_now_vendor)
        )

        finish()  // Closes the current activity
    }  // end of upgradedToVendorPrompt method

}  // end of BecomeVendorActivity class