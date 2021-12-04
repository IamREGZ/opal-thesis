package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentChangePasswordBinding
import edu.cccdci.opal.utils.UtilityClass

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var mUtility: UtilityClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater)

        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        // Verify password change once it is clicked
        binding.btnChangePass.setOnClickListener { verifyPassword() }

        return binding.root
    }  // end of onCreateView method

    // Function to validate password change
    private fun validateChangePass(): Boolean {
        with(binding) {
            return when {
                // If Current Password Field is empty
                TextUtils.isEmpty(etCurrentPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_current_password),
                        true
                    )
                    false  // return false
                }

                // If New Password Field is empty
                TextUtils.isEmpty(etNewPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_new_password),
                        true
                    )
                    false  // return false
                }

                // If Confirm New Password Field is empty
                TextUtils.isEmpty(etConfNewPass.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_confirm_password),
                        true
                    )
                    false  // return false
                }

                // If New Password and Confirm New Password do not match
                etNewPass.text.toString().trim { it <= ' ' } !=
                        etConfNewPass.text.toString().trim { it <= ' ' } -> {
                    // Display an error message
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_passwords_not_match),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all inputs are valid
            } // end of when
        }  // end of with(binding)
    }  // end of validateChangePass method

    // Function to verify current password, and then change password
    private fun verifyPassword() {
        with(binding) {
            // Validate first the Change Password inputs
            if (validateChangePass()) {
                // Display the loading message
                mUtility.showProgressDialog(
                    requireContext(),
                    requireActivity(),
                    resources.getString(R.string.msg_please_wait)
                )

                // Get the inputted current and new password
                val currentPass = etCurrentPass.text.toString().trim { it <= ' ' }
                val newPass = etNewPass.text.toString().trim { it <= ' ' }

                // Get credentials of the current user
                val credential = EmailAuthProvider.getCredential(
                    FirebaseAuth.getInstance().currentUser!!.email!!, currentPass
                )

                // Verify credentials
                FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        // Correct credentials
                        if (task.isSuccessful) {
                            changePassword(newPass)  // Proceed to change password
                        } else {
                            // Wrong credentials

                            // Hide the loading message
                            mUtility.hideProgressDialog()

                            // Display the error message
                            mUtility.showSnackBar(
                                requireActivity(),
                                task.exception!!.message.toString(),
                                true
                            )
                        }
                    }  // end of reauthenticate
            }  // end of if

        }  // end of with(binding)
    }  // end of verifyPassword method

    // Function to change password
    private fun changePassword(newPassword: String) {
        // Change user password from Firebase
        FirebaseAuth.getInstance().currentUser!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                // Successful task
                if (task.isSuccessful) {
                    mUtility.hideProgressDialog()  // Hide the loading message

                    // Display a Toast message
                    mUtility.toastMessage(
                        requireContext(),
                        resources.getString(R.string.msg_pwd_change_success)
                    )

                    // Sends the user back to the previous fragment
                    requireActivity().onBackPressed()
                } else {
                    // If it is not successful
                    mUtility.hideProgressDialog()  // Hide the loading message

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(),
                        task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of updatePassword
    }  // end of changePassword method

}  // end of ChangePasswordFragment class