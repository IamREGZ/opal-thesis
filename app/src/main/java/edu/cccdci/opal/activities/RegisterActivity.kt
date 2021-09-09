package edu.cccdci.opal.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityRegisterBinding

class RegisterActivity : TemplateActivity(), View.OnClickListener {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        with(binding) {
            setContentView(root)

            //Click event for Register Button
            btnRegister.setOnClickListener(this@RegisterActivity)
            //Click event for Cancel Button
            btnCancel.setOnClickListener(this@RegisterActivity)
        } //end of with(binding)

    } //end of onCreate method

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Proceeds to the account registration
                R.id.btn_register -> {
                    registerUser()
                }

                //Goes back to the Login Screen
                R.id.btn_cancel -> {
                    onBackPressed()
                }
            } //end of when

        } //end of if

    } //end of onClick method

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
            TextUtils.isEmpty(etRegisterConfPass.text.toString().trim { it <= ' ' }) -> {
                showMessagePrompt(
                    resources.getString(R.string.err_blank_confirm_password), true
                )
                false
            }

            //If Password and Confirm Password do not match
            etRegisterPass.text.toString().trim { it <= ' ' } !=
                    etRegisterConfPass.text.toString().trim { it <= ' ' } -> {
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

            else -> true //If all user inputs are valid
        } //end of when

    } //end of registerValidation method

    //Function to register user account
    private fun registerUser() {
        with(binding) {
            //Validate first the registration inputs
            if (registerValidation()) {

                //Display the loading message
                showProgressDialog(resources.getString(R.string.msg_please_wait))

                val email: String = etRegisterEmail.text
                    .toString().trim { it <= ' ' }
                val password: String = etRegisterPass.text
                    .toString().trim { it <= ' ' }

                //Create a Firebase Authentication using Email and Password
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        hideProgressDialog() //Hide the loading message

                        //Successful task
                        if (task.isSuccessful) {
                            //val firebaseUser: FirebaseUser = task.result!!.user!!
                            //showMessagePrompt(resources.getString(R.string.msg_register_success), false)

                            //Displays a toast message
                            Toast.makeText(
                                this@RegisterActivity,
                                resources.getString(R.string.msg_register_success),
                                Toast.LENGTH_LONG
                            ).show()

                            FirebaseAuth.getInstance().signOut()
                            finish() //Close the activity

                        } else {
                            //If it is not successful
                            showMessagePrompt(
                                task.exception!!.message.toString(), true
                            )
                        }
                    } //end of createUserWithEmailAndPassword

            } //end of if

        } //end of with(binding)

    } //end of registerUser method

} //end of RegisterActivity class