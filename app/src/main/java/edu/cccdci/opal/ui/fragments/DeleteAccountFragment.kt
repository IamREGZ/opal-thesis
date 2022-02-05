package edu.cccdci.opal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentDeleteAccountBinding
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.ui.activities.LoginActivity
import edu.cccdci.opal.utils.UtilityClass

class DeleteAccountFragment : Fragment() {

    private lateinit var binding: FragmentDeleteAccountBinding
    private lateinit var mUtility: UtilityClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeleteAccountBinding.inflate(inflater)

        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        with(binding) {
            btnConfAccDel.setOnClickListener {
                // Get the inputted user password
                val password: String = etDelAccPass.text.toString()
                    .trim { it <= ' ' }

                if (password.isEmpty()) {
                    // Error if password field is empty
                    mUtility.showSnackBar(
                        requireActivity(), getString(R.string.err_blank_password),
                        true
                    )
                } else {
                    // Proceed with account deletion
                    verifyCredentials(password)
                }
            }  // end of setOnClickListener

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Function to verify credentials, and then delete user account
    private fun verifyCredentials(password: String) {
        // Display the loading message
        mUtility.showProgressDialog(
            requireContext(), requireActivity(), getString(R.string.msg_please_wait)
        )

        // Get credentials of the current user
        val credential = EmailAuthProvider.getCredential(
            FirebaseAuth.getInstance().currentUser!!.email!!, password
        )

        // Verify credentials
        FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
            .addOnCompleteListener { task ->
                // Correct credentials
                if (task.isSuccessful) {
                    // Delete data from Firestore Database
                    FirestoreClass().deleteUserData(
                        this@DeleteAccountFragment, mUtility
                    )
                } else {
                    // Wrong credentials
                    mUtility.hideProgressDialog()  // Hide the loading message

                    // Clear the password field for security purposes
                    binding.etDelAccPass.text!!.clear()

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(), task.exception!!.message.toString(),
                        true
                    )
                }
            } // end of reauthenticate

    }  // end of verifyCredentials method

    // Function to delete account from Firebase Authentication
    fun deleteUserAccount() {
        // Delete the current user's account
        FirebaseAuth.getInstance().currentUser!!.delete()
            .addOnCompleteListener { task ->
                // Successful task
                if (task.isSuccessful) {
                    // Create an Intent to launch LoginActivity
                    val intent = Intent(
                        context, LoginActivity::class.java
                    )
                    /* To ensure that no more activity layers are active
                     * after the account was deleted
                     */
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    // Display a Toast message
                    mUtility.toastMessage(
                        requireContext(),
                        resources.getString(R.string.msg_acc_delete_success)
                    )

                    startActivity(intent)  // Opens the login page
                    requireActivity().finish()  // Closes the current activity
                } else {
                    // If it is not successful
                    mUtility.hideProgressDialog()  // Hide the loading message

                    // Clear the password field for security purposes
                    binding.etDelAccPass.text!!.clear()

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(), task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of delete

    }  // end of deleteUserAccount method

}  // end of DeleteAccountFragment class