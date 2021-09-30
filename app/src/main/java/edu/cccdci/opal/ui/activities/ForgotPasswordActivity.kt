package edu.cccdci.opal.ui.activities

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : TemplateActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            //Setups the Action Bar of the current activity
            setupActionBar(tlbForgotPwActivity, true)

            btnSubmitRecoveryEmail.setOnClickListener {
                val email: String = etRecoveryEmail.text.toString()
                    .trim { it <= ' ' }

                if (email.isEmpty()) {
                    //Error if email field is empty
                    showMessagePrompt(
                        resources.getString(R.string.err_blank_email), true
                    )
                } else {
                    //Proceed with recovery email submission
                    submitRecoveryEmail(email)
                }
            } //end of setOnClickListener

        } //end of with(binding)

    } //end of onCreate method

    //Function to initiate email recovery process
    private fun submitRecoveryEmail(email: String) {
        //Display the loading message
        showProgressDialog(resources.getString(R.string.msg_please_wait))

        //Firebase Authentication to send reset password email
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->

                hideProgressDialog() //Hides the loading message

                //Successful task
                if (task.isSuccessful) {
                    //Displays a toast message
                    toastMessage(
                        this@ForgotPasswordActivity,
                        resources.getString(R.string.msg_recovery_email_submitted),
                        true
                    ).show()

                    finish() //Closes the activity
                } else {
                    //If it is not successful
                    showMessagePrompt(task.exception!!.message.toString(), true)
                }

            } //end of sendPasswordResetEmail

    } //end of submitRecoveryEmail method

} //end of ForgotPasswordActivity class