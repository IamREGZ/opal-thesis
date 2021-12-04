package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityRegisterBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.UtilityClass

class RegisterActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mUserEmail: String
    private lateinit var mUserPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // Click event for Register Button
            btnRegister.setOnClickListener(this@RegisterActivity)
            // Click event for Cancel Button
            btnCancel.setOnClickListener(this@RegisterActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Proceeds to the account registration
                R.id.btn_register -> {
                    registerUser()
                }

                // Goes back to the Login Screen
                R.id.btn_cancel -> {
                    onBackPressed()
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Function to validate user registration
    private fun registerValidation(): Boolean {
        with(binding) {
            return when {
                // If the First Name field is empty
                TextUtils.isEmpty(etRegisterFname.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_fname),
                        true
                    )
                    false  // return false
                }

                // If the Last Name field is empty
                TextUtils.isEmpty(etRegisterLname.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_lname),
                        true
                    )
                    false  // return false
                }

                // If the Email field is empty
                TextUtils.isEmpty(etRegisterEmail.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_email),
                        true
                    )
                    false  // return false
                }

                // If the Username field is empty
                TextUtils.isEmpty(etRegisterUser.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_username),
                        true
                    )
                    false  // return false
                }

                // If the Password field is empty
                TextUtils.isEmpty(etRegisterPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_password),
                        true
                    )
                    false  // return false
                }

                // If the Confirm Password field is empty
                TextUtils.isEmpty(etRegisterConfPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_blank_confirm_password),
                        true
                    )
                    false  // return false
                }

                // If Password and Confirm Password do not match
                etRegisterPass.text.toString().trim { it <= ' ' } !=
                        etRegisterConfPass.text.toString().trim { it <= ' ' } -> {
                    //Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_passwords_not_match),
                        true
                    )
                    false  // return false
                }

                // If the T&C checkbox is not checked
                !cbTermsAndConditions.isChecked -> {
                    // Display an error message
                    showSnackBar(
                        this@RegisterActivity,
                        resources.getString(R.string.err_unchecked_tac),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all user inputs are valid
            }  // end of when
        }  // end of with(binding)

    }  // end of registerValidation method

    // Function to register user account
    private fun registerUser() {
        with(binding) {
            // Validate first the registration inputs
            if (registerValidation()) {
                // Display the loading message
                showProgressDialog(
                    this@RegisterActivity,
                    this@RegisterActivity,
                    resources.getString(R.string.msg_please_wait)
                )

                // Get the inputted email and password
                mUserEmail = etRegisterEmail.text.toString().trim { it <= ' ' }
                mUserPassword = etRegisterPass.text.toString().trim { it <= ' ' }

                // Create a Firebase Authentication using Email and Password
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(mUserEmail, mUserPassword)
                    .addOnCompleteListener { task ->
                        // Successful task
                        if (task.isSuccessful) {
                            val fBaseUser: FirebaseUser = task.result!!.user!!

                            // Object to store user data
                            val user = User(
                                fBaseUser.uid,
                                etRegisterFname.text.toString().trim { it <= ' ' },
                                etRegisterLname.text.toString().trim { it <= ' ' },
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
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mUserEmail, mUserPassword)
            .addOnCompleteListener { task ->
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
                        resources.getString(R.string.err_register_login_failed)
                    )
                    finish()  // Closes the activity
                }
            }  // end of signInWithEmailAndPassword

    }  // end of registerSuccessPrompt method

    // Function to send user to the home page after a successful log in
    fun firstLogInPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Create an Intent to launch MainActivity
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)

        /* To clear the current stack of activities so that when the user presses
         * back from the home activity, it will exit the application instead of
         * going to log in activity.
         */
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Displays a toast message
        toastMessage(
            this@RegisterActivity,
            resources.getString(R.string.msg_register_success),
            false
        )

        startActivity(intent)  // Opens the main activity
        finish()  // Closes the activity
    }  // end of firstLogInPrompt method

}  // end of RegisterActivity class