package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityForgotPasswordBinding
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class ForgotPasswordActivity : UtilityClass() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbForgotPwActivity)

            // Actions when the Submit button is clicked
            btnSubmitRecoveryEmail.setOnClickListener {
                // Validate first the email
                if (FormValidation(this@ForgotPasswordActivity)
                        .validateEmail(etRecoveryEmail)
                ) {
                    // Proceed with recovery email submission if it is valid
                    submitRecoveryEmail(
                        etRecoveryEmail.text.toString().trim { it <= ' ' }
                    )
                }  // end of if
            }
        }  // end of with(binding)

    }  // end of onCreate method

    // Function to initiate email recovery process
    private fun submitRecoveryEmail(email: String) {
        // Display the loading message
        showProgressDialog(
            this@ForgotPasswordActivity, this@ForgotPasswordActivity,
            resources.getString(R.string.msg_please_wait)
        )

        // Firebase Authentication to send reset password email
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                hideProgressDialog()  // Hides the loading message

                // Successful task
                if (task.isSuccessful) {
                    // Displays a toast message
                    toastMessage(
                        this@ForgotPasswordActivity,
                        resources.getString(R.string.msg_recovery_email_submitted),
                        true
                    )
                    finish()  // Closes the activity
                } else {
                    // If it is not successful
                    showSnackBar(
                        this@ForgotPasswordActivity,
                        task.exception!!.message.toString(),
                        true
                    )
                }  // end of if-else
            }  // end of sendPasswordResetEmail

    }  // end of submitRecoveryEmail method

}  // end of ForgotPasswordActivity class