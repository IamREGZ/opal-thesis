package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityRegisterBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class RegisterActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbRegisterActivity, false)

            // Click event for Register Button
            btnRegister.setOnClickListener(this@RegisterActivity)
            // Click event for Log In TextView
            tvLoginLink.setOnClickListener(this@RegisterActivity)

            // Enables observer for a strong password
            FormValidation(this@RegisterActivity).passwordObserver(
                etRegisterPass,
                /* In order: Eight characters, lowercase, uppercase, digits &
                 * special character
                 */
                listOf(
                    ivRegPassEightChars, ivRegPassLowercase, ivRegPassUppercase,
                    ivRegPassDigits, ivRegPassSpecialChars
                )
            )
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Proceeds to the account registration
                R.id.btn_register -> registerUser()

                // Goes back to the Login Screen
                R.id.tv_login_link -> onBackPressed()
            }  // end of when
        }  // end of if

    }  // end of onClick method

    // Function to validate user registration
    private fun registerValidation(): Boolean {
        with(binding) {
            // Create a FormValidation object, and then execute the validations
            return FormValidation(this@RegisterActivity).run {
                when {
                    // First Name
                    !validatePersonName(etRegisterFirstName) -> false
                    // Last Name
                    !validatePersonName(etRegisterLastName) -> false
                    // Email Address
                    !validateEmail(etRegisterEmail) -> false
                    // Username
                    !validateUsername(etRegisterUser) -> false
                    // Password
                    !validatePassword(etRegisterPass) -> false
                    // Confirm Password
                    !confirmPassword(etRegisterPass, etRegisterConfPass) -> false
                    // Terms and Conditions
                    !requiredCheckbox(cbTermsAndConditions) -> false
                    // When all fields are valid
                    else -> true
                }  // end of when
            }  // end of run

        }  // end of with(binding)

    }  // end of registerValidation method

    // Function to register user account
    private fun registerUser() {
        with(binding) {
            // Validate first the registration inputs
            if (registerValidation()) {
                // Display the loading message
                showProgressDialog(
                    this@RegisterActivity, this@RegisterActivity,
                    getString(R.string.msg_please_wait)
                )

                // Create a Firebase Authentication using Email and Password
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        etRegisterEmail.text.toString().trim { it <= ' ' },
                        etRegisterPass.text.toString().trim { it <= ' ' }
                    ).addOnCompleteListener { task ->
                        // Successful task
                        if (task.isSuccessful) {
                            // Object to store user data
                            val user = User(
                                task.result!!.user!!.uid,
                                etRegisterFirstName.text.toString().trim { it <= ' ' },
                                etRegisterLastName.text.toString().trim { it <= ' ' },
                                etRegisterEmail.text.toString().trim { it <= ' ' },
                                etRegisterUser.text.toString().trim { it <= ' ' }
                            )

                            // Adds user data in the Firestore Database
                            FirestoreClass().registerUser(
                                this@RegisterActivity, user
                            )
                        } else {
                            // If it is not successful
                            hideProgressDialog()  // Hide the loading message

                            // Display an error message
                            showSnackBar(
                                this@RegisterActivity,
                                task.exception!!.message.toString(),
                                true
                            )
                        }
                    }  // end of createUserWithEmailAndPassword
            }  // end of if

        }  // end of with(binding)

    }  // end of registerUser method

    // Function to prompt user that after he/she was registered, proceed to logging in
    fun registerSuccessPrompt() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            binding.etRegisterEmail.text.toString().trim { it <= ' ' },
            binding.etRegisterPass.text.toString().trim { it <= ' ' }
        ).addOnCompleteListener { task ->
            // Successful task
            if (task.isSuccessful) {
                // Gets the user details
                FirestoreClass().getUserDetails(this@RegisterActivity)
            } else {
                // If it is not successful
                hideProgressDialog()  // Hide the loading message

                // Displays a toast message
                toastMessage(
                    this@RegisterActivity,
                    getString(R.string.err_register_login_failed)
                )
                finish()  // Closes the activity
            }
        }  // end of signInWithEmailAndPassword

    }  // end of registerSuccessPrompt method

    // Function to send user to the home page after a successful log in
    fun firstLogInPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Create an Intent to launch MainActivity
        Intent(this@RegisterActivity, MainActivity::class.java).apply {
            /* To clear the current stack of activities so that when the user
             * presses back from the home activity, it will exit the application
             * instead of going to log in activity.
             */
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Displays a toast message
            toastMessage(
                this@RegisterActivity,
                getString(R.string.msg_register_success),
                false
            )

            startActivity(this)  // Opens the main activity
            finish()  // Closes the activity
        }  // end of let

    }  // end of firstLogInPrompt method

}  // end of RegisterActivity class
