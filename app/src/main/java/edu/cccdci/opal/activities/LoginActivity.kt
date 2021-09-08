package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityLoginBinding

class LoginActivity : MessageActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

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
        }

    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Logins the user and sends to home page
                R.id.btn_login -> {
                    // TODO: Login User and go to Home Page
                    loginUser()
                }

                //Open the Forgot Password Activity
                R.id.tv_forgot_password -> {
                    // TODO: Forgot Password
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

            }

        }

    }

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
        }
    }

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

                        hideProgressDialog() //Hide the loading message

                        //Successful task
                        if (task.isSuccessful) {
                            // TODO: Go to home page
                            showMessagePrompt("Logged in successfully", false)
                        } else {
                            //If it is not successful
                            showMessagePrompt(task.exception!!.message.toString(), true)
                        }
                    }

            }

        }

    }

}