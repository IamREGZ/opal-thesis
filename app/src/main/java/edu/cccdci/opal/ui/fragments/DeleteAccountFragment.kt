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
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.FormValidation
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
                // If password is not empty, proceed with account deletion
                if (FormValidation(requireActivity())
                        .validateAuthPassword(etDelAccPass)
                ) {
                    /* Display an alert dialog with two action buttons
                     * (Delete & Cancel)
                     */
                    DialogClass(requireContext(), this@DeleteAccountFragment)
                        .alertDialog(
                            getString(R.string.dialog_delete_account_title),
                            getString(R.string.dialog_delete_account_message),
                            getString(R.string.dialog_btn_delete),
                            getString(R.string.dialog_btn_cancel)
                        )
                }
            }

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Function to verify credentials, and then delete user account
    internal fun verifyCredentials() {
        // Display the loading message
        mUtility.showProgressDialog(
            requireContext(), requireActivity(), getString(R.string.msg_please_wait)
        )

        // Get credentials of the current user
        val credential = EmailAuthProvider.getCredential(
            FirebaseAuth.getInstance().currentUser!!.email!!,
            binding.etDelAccPass.text.toString().trim { it <= ' ' }
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
                }
                // Wrong credentials
                else {
                    mUtility.hideProgressDialog()  // Hide the loading message

                    clearPassword()  // Clear the password field

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(), task.exception!!.message.toString(),
                        true
                    )
                }
            } // end of reauthenticate

    }  // end of verifyCredentials method

    // Function to delete account from Firebase Authentication
    internal fun deleteUserAccount() {
        // Delete the current user's account
        FirebaseAuth.getInstance().currentUser!!.delete()
            .addOnCompleteListener { task ->
                // Successful task
                if (task.isSuccessful) {
                    // Create an Intent to launch LoginActivity
                    Intent(context, LoginActivity::class.java).run {
                        /* To ensure that no more activity layers are active
                         * after the account was deleted
                         */
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                        // Display a Toast message
                        mUtility.toastMessage(
                            requireContext(),
                            getString(R.string.msg_acc_delete_success)
                        )

                        startActivity(this)  // Opens the login page
                        requireActivity().finish()  // Closes the current activity
                    }
                }
                // If it is not successful
                else {
                    mUtility.hideProgressDialog()  // Hide the loading message

                    clearPassword()  // Clear the password field

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(), task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of delete

    }  // end of deleteUserAccount method

    // Function to clear the password text field for security purposes
    internal fun clearPassword() {
        if (binding.etDelAccPass.text!!.isNotEmpty())
            binding.etDelAccPass.text!!.clear()
    }  // end of clearPassword method

}  // end of DeleteAccountFragment class
