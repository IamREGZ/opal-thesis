package edu.cccdci.opal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentDeleteAccountBinding
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.ui.activities.LoginActivity

class DeleteAccountFragment : TemplateFragment() {

    private lateinit var binding: FragmentDeleteAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeleteAccountBinding.inflate(inflater)

        with(binding) {

            btnConfAccDel.setOnClickListener {

                val password: String = etDelAccPass.text.toString()
                    .trim { it <= ' ' }

                if (password.isEmpty()) {
                    //Error if password field is empty
                    showMessagePrompt(
                        resources.getString(R.string.err_blank_password),
                        true
                    )
                } else {
                    //Proceed with account deletion
                    verifyCredentials(password)
                }

            } //end of setOnClickListener

            return root
        } //end of with(binding)

    } //end of onCreateView method

    //Function to verify credentials, and then delete user account
    private fun verifyCredentials(password: String) {
        //Display the loading message
        showProgressDialog(resources.getString(R.string.msg_please_wait))

        //Get credentials of the current user
        val credential = EmailAuthProvider.getCredential(
            FirebaseAuth.getInstance().currentUser!!.email!!, password
        )

        //Verify credentials
        FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
            .addOnCompleteListener { task ->

                //Correct credentials
                if (task.isSuccessful) {
                    //Delete data from Firestore Database
                    FirestoreClass().deleteUserData(this@DeleteAccountFragment)
                } else {
                    //Wrong credentials

                    hideProgressDialog() //Hide the loading message

                    //Clear the password field
                    binding.etDelAccPass.text!!.clear()

                    //Display the error message
                    showMessagePrompt(
                        task.exception!!.message.toString(), true
                    )
                }

            } //end of reauthenticate

    } //end of verifyCredentials method

    //Function to delete account from Firebase
    fun deleteUserAccount() {
        //Delete the current user's account
        FirebaseAuth.getInstance().currentUser!!.delete()
            .addOnCompleteListener { task ->

                //Successful task
                if (task.isSuccessful) {

                    //Create an Intent to launch LoginActivity
                    val intent = Intent(
                        context, LoginActivity::class.java
                    )

                    /* To ensure that no more activity layers are active
                     * after the account was deleted
                     */
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    //Display a Toast message
                    toastMessage(
                        resources.getString(R.string.msg_acc_delete_success)
                    ).show()

                    startActivity(intent) //Opens the login page
                    requireActivity().finish() //Closes the current activity

                } else {
                    //If it is not successful

                    hideProgressDialog() //Hide the loading message

                    //Display the error message
                    showMessagePrompt(
                        task.exception!!.message.toString(), true
                    )
                }

        } //end of delete

    } //end of deleteUserAccount method

} //end of DeleteAccountFragment class