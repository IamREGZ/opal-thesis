package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityRegisterBinding

class RegisterActivity : MessageActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        with(binding) {
            setContentView(root)

            btnRegister.setOnClickListener {
                registerValidation() //Validates user's registration
            }

            btnCancel.setOnClickListener {
                //Open the Launch Activity
                startActivity(
                    Intent(this@RegisterActivity, LoginActivity::class.java)
                )
                finish() //Closes the current activity
            }

        }

    }

    //Function to validate user registration
    private fun registerValidation(): Boolean = with(binding) {
        when {
            //If the First Name field is empty
            TextUtils.isEmpty(etRegisterFname.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_fname), true
                )
                false
            }

            //If the Last Name field is empty
            TextUtils.isEmpty(etRegisterLname.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_lname), true
                )
                false
            }

            //If the Email field is empty
            TextUtils.isEmpty(etRegisterEmail.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_email), true
                )
                false
            }

            //If the Username field is empty
            TextUtils.isEmpty(etRegisterUser.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_username), true
                )
                false
            }

            //If the Password field is empty
            TextUtils.isEmpty(etRegisterPass.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_password), true
                )
                false
            }

            //If the Confirm Password field is empty
            TextUtils.isEmpty(etRegisterConfpass.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_confirm_password), true
                )
                false
            }

            //If Password and Confirm Password do not match
            etRegisterPass.text.toString().trim { it <= ' ' } !=
                    etRegisterConfpass.text.toString().trim { it <= ' ' } -> {
                showMessagePrompt(
                    resources.getString(R.string.err_passwords_not_match), true
                )
                false
            }

            //If the T&C checkbox is not checked
            !cbTermsAndConditions.isChecked -> {
                showMessagePrompt(
                    resources.getString(R.string.err_unchecked_tac), true
                )
                false
            }

            //If all user inputs are valid
            else -> {
                showMessagePrompt(
                    resources.getString(R.string.msg_register_success), false
                )
                true
            }

        }

    }

}