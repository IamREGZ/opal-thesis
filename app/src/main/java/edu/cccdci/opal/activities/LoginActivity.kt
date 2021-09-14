package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityLoginBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass

class LoginActivity : TemplateActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //Force disables dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            //Click event for Login Button
            btnLogin.setOnClickListener(this@LoginActivity)
            //Click event for Forgot Password TextView
            tvForgotPassword.setOnClickListener(this@LoginActivity)
            //Click event for Register TextView
            tvRegister.setOnClickListener(this@LoginActivity)
        } //end of with(binding)

    } //end of onCreate method

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Logins the user and sends to home page
                R.id.btn_login -> {
                    loginUser()
                }

                //Open the Forgot Password Activity
                R.id.tv_forgot_password -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            ForgotPasswordActivity::class.java
                        )
                    )
                }

                //Open the Register Activity
                R.id.tv_register -> {
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            RegisterActivity::class.java
                        )
                    )
                }

            } //end of when

        } //end of if

    } //end of onClick method

    //Function to validate login information
    private fun validateLogin(): Boolean = with(binding) {
        when {
            //If the Email field is empty
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(resources.getString(R.string.err_blank_email), true)
                false
            }

            //If the Password field is empty
            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(resources.getString(R.string.err_blank_password), true)
                false
            }

            else -> true //If all inputs are valid
        } //end of when

    } //end of validateLogin method

    //Function to login user
    private fun loginUser() {
        with(binding) {
            //Validate first the login inputs
            if (validateLogin()) {

                //Display the loading message
                showProgressDialog(resources.getString(R.string.msg_please_wait))

                val email = etEmail.text.toString().trim { it <= ' ' }
                val password = etPassword.text.toString().trim { it <= ' ' }

                //Authenticate Firebase Account using Email and Password
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        //Successful task
                        if (task.isSuccessful) {
                            FirestoreClass().getUserDetails(this@LoginActivity)
                        } else {
                            //If it is not successful

                            hideProgressDialog() //Hide the loading message

                            //Clear the text fields
                            etEmail.text?.clear()
                            etPassword.text?.clear()

                            showMessagePrompt(task.exception!!.message.toString(), true)
                        }
                    } //end of signInWithEmailAndPassword

            } //end of if

        } //end of with(binding)

    } //end of loginUser method

    fun logInSuccessPrompt(user: User) {

        hideProgressDialog() //Hide the loading message

        //Log the user details in the console
        Log.i("First Name: ", user.firstName)
        Log.i("Last Name: ", user.lastName)
        Log.i("Email Address: ", user.emailAdd)
        Log.i("Username: ", user.userName)

        //Displays a Toast message
        longToastMessage(
            this@LoginActivity,
            resources.getString(R.string.msg_login_success)
        ).show()

        //Opens the home page
        startActivity(
            Intent(
                this@LoginActivity,
                MainActivity::class.java
            )
        )
        finish() //Closes the current activity

    } //end of logInSuccessPrompt method

} //end of LoginActivity class