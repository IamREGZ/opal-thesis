package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityLoginBinding
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class LoginActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // Click event for Login Button
            btnLogIn.setOnClickListener(this@LoginActivity)
            // Click event for Forgot Password TextView
            tvForgotPasswordLink.setOnClickListener(this@LoginActivity)
            // Click event for Register TextView
            tvRegisterLink.setOnClickListener(this@LoginActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Logins the user and sends to home page
                R.id.btn_log_in -> loginUser()

                // Open the Forgot Password Activity
                R.id.tv_forgot_password_link -> startActivity(
                    Intent(
                        this@LoginActivity, ForgotPasswordActivity::class.java
                    )
                )

                // Open the Register Activity
                R.id.tv_register_link -> startActivity(
                    Intent(
                        this@LoginActivity, RegisterActivity::class.java
                    )
                )
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Operations to do when this activity is visible again
    override fun onRestart() {
        super.onRestart()
        clearLoginFields()  // Clears the login fields for security purposes
    }  // end of onRestart method

    // Override the function to make the user double press back to exit
    override fun onBackPressed() {
        doubleBackToExit()
    }  // end of onBackPressed method

    // Function to validate login information
    private fun validateLogin(): Boolean {
        with(binding) {
            // Create a FormValidation object, and then execute the validations
            return FormValidation(this@LoginActivity).run {
                when {
                    // Email Address
                    !validateEmail(etLoginEmail) -> false
                    // Password
                    !validateAuthPassword(etLoginPass) -> false
                    // When all fields are valid
                    else -> true
                }  // end of when
            }  // end of run

        }  // end of with(binding)

    }  // end of validateLogin method

    // Function to login user
    private fun loginUser() {
        with(binding) {
            // Validate first the login inputs
            if (validateLogin()) {
                // Display the loading message
                showProgressDialog(
                    this@LoginActivity, this@LoginActivity,
                    getString(R.string.msg_logging_in)
                )

                // Authenticate Firebase Account using Email and Password
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    etLoginEmail.text.toString().trim { it <= ' ' },
                    etLoginPass.text.toString().trim { it <= ' ' }
                ).addOnCompleteListener { task ->
                    // Successful task
                    if (task.isSuccessful) {
                        // Gets the user details
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    } else {
                        // If it is not successful
                        hideProgressDialog()  // Hide the loading message

                        clearLoginFields()  // Clears the login fields

                        // Display an error message
                        showSnackBar(
                            this@LoginActivity,
                            task.exception!!.message.toString(),
                            true
                        )
                    }
                }  // end of signInWithEmailAndPassword

            }  // end of if
        }  // end of with(binding)

    }  // end of loginUser method

    // Function to prompt that he/she is logged in
    fun logInSuccessPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Opens the home page
        startActivity(
            Intent(
                this@LoginActivity, MainActivity::class.java
            )
        )
        finish()  // Closes the current activity
    }  // end of logInSuccessPrompt method

    // Function to clear the login text fields for security purposes
    private fun clearLoginFields() {
        with(binding) {
            if (etLoginEmail.text!!.isNotEmpty()) etLoginEmail.text!!.clear()
            if (etLoginPass.text!!.isNotEmpty()) etLoginPass.text!!.clear()
        }  // end of with(binding)
    }  // end of clearLoginFields method

}  // end of LoginActivity class
