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

            //Click event for Login Button
            btnSignIn.setOnClickListener(this@LoginActivity)
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
                R.id.btn_sign_in -> {
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

    //Override the function to make the user double press back to exit
    override fun onBackPressed() {
        doubleBackToExit()
    } //end of onBackPressed method

    //Function to validate login information
    private fun validateLogin(): Boolean = with(binding) {
        when {
            //If the Email field is empty
            TextUtils.isEmpty(etLoginEmail.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(resources.getString(R.string.err_blank_email), true)
                false
            }

            //If the Password field is empty
            TextUtils.isEmpty(etLoginPass.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(resources.getString(R.string.err_blank_password), true)
                false
            }

            else -> true //If all inputs are valid
        } //end of when

    } //end of with(binding) and validateLogin method

    //Function to login user
    private fun loginUser() {
        with(binding) {
            //Validate first the login inputs
            if (validateLogin()) {

                //Display the loading message
                showProgressDialog(resources.getString(R.string.msg_signing_in))

                val email = etLoginEmail.text.toString().trim { it <= ' ' }
                val password = etLoginPass.text.toString().trim { it <= ' ' }

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
                            etLoginEmail.text?.clear()
                            etLoginPass.text?.clear()

                            showMessagePrompt(task.exception!!.message.toString(), true)
                        }
                    } //end of signInWithEmailAndPassword

            } //end of if

        } //end of with(binding)

    } //end of loginUser method

    //Function to prompt that he/she is logged in
    fun logInSuccessPrompt() {

        hideProgressDialog() //Hide the loading message

        startActivity(Intent(
            this@LoginActivity, HomeActivity::class.java)
        ) //Opens the home page
        finish() //Closes the current activity

    } //end of logInSuccessPrompt method

} //end of LoginActivity class