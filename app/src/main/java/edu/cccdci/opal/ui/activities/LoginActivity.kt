package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityLoginBinding
import edu.cccdci.opal.firestore.FirestoreClass

class LoginActivity : TemplateActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // Click event for Login Button
            btnLogIn.setOnClickListener(this@LoginActivity)
            // Click event for Forgot Password TextView
            tvForgotPassword.setOnClickListener(this@LoginActivity)
            // Click event for Register TextView
            tvRegister.setOnClickListener(this@LoginActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Logins the user and sends to home page
                R.id.btn_log_in -> {
                    loginUser()
                }

                // Open the Forgot Password Activity
                R.id.tv_forgot_password -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            ForgotPasswordActivity::class.java
                        )
                    )
                }

                // Open the Register Activity
                R.id.tv_register -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            RegisterActivity::class.java
                        )
                    )
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Operations to do when this activity is active again
    override fun onResume() {
        super.onResume()
        clearLoginFields()  // Clears the login fields for security purposes
    }  // end of onResume method

    // Override the function to make the user double press back to exit
    override fun onBackPressed() {
        doubleBackToExit()
    }  // end of onBackPressed method

    // Function to validate login information
    private fun validateLogin(): Boolean {
        with(binding) {
            return when {
                // If the Email field is empty
                TextUtils.isEmpty(etLoginEmail.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showMessagePrompt(resources.getString(R.string.err_blank_email), true)
                    false  // return false
                }

                // If the Password field is empty
                TextUtils.isEmpty(etLoginPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showMessagePrompt(resources.getString(R.string.err_blank_password), true)
                    false  // return false
                }

                else -> true  // If all inputs are valid
            }  // end of when
        }  // end of with(binding)

    }  // end of validateLogin method

    // Function to login user
    private fun loginUser() {
        with(binding) {
            // Validate first the login inputs
            if (validateLogin()) {
                // Display the loading message
                showProgressDialog(resources.getString(R.string.msg_logging_in))

                // Get the inputted email and password
                val email = etLoginEmail.text.toString().trim { it <= ' ' }
                val password = etLoginPass.text.toString().trim { it <= ' ' }

                // Authenticate Firebase Account using Email and Password
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        // Successful task
                        if (task.isSuccessful) {
                            // Gets the user details
                            FirestoreClass().getUserDetails(this@LoginActivity)
                        } else {
                            // If it is not successful
                            hideProgressDialog()  // Hide the loading message

                            clearLoginFields()  // Clears the login fields

                            // Display an error message
                            showMessagePrompt(task.exception!!.message.toString(), true)
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

    // Function to clear the login text fields
    private fun clearLoginFields() {
        with(binding) {
            if (etLoginEmail.text!!.isNotEmpty())
                etLoginEmail.text!!.clear()

            if (etLoginPass.text!!.isNotEmpty())
                etLoginPass.text!!.clear()
        }
    }  // end of clearLoginFields method

}  // end of LoginActivity class