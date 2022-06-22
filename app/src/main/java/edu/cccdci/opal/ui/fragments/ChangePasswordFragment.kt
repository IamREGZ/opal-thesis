package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentChangePasswordBinding
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var mUtility: UtilityClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater)

        with(binding) {
            // Enables observer for a strong password
            FormValidation(requireActivity()).passwordObserver(
                etNewPass,
                /* In order: Eight characters, lowercase, uppercase, digits &
                 * special character
                 */
                listOf(
                    ivNewPassEightChars, ivNewPassLowercase, ivNewPassUppercase,
                    ivNewPassDigits, ivNewPassSpecialChars
                )
            )

            // Verify password change once it is clicked
            btnChangePass.setOnClickListener { verifyPassword() }
        }

        return binding.root
    }  // end of onCreateView method

    // Function to validate password change
    private fun validateChangePass(): Boolean {
        with(binding) {

            return FormValidation(requireActivity()).run {
                when {
                    // Current Password
                    !validateAuthPassword(etCurrentPass) -> false
                    // New Password
                    !validatePassword(etNewPass) -> false
                    // Confirm New Password
                    !confirmPassword(etNewPass, etConfNewPass) -> false
                    // When all fields are valid
                    else -> true
                }  // end of when
            }  // end of run

        }  // end of with(binding)

    }  // end of validateChangePass method

    // Function to verify current password, and then change password
    private fun verifyPassword() {
        // Validate first the Change Password inputs
        if (validateChangePass()) {
            // Display the loading message
            mUtility.showProgressDialog(
                requireContext(), requireActivity(),
                getString(R.string.msg_please_wait)
            )

            // Get credentials of the current user
            val credential = EmailAuthProvider.getCredential(
                FirebaseAuth.getInstance().currentUser!!.email!!,
                binding.etCurrentPass.text.toString().trim { it <= ' ' }
            )

            // Verify credentials
            FirebaseAuth.getInstance().currentUser!!.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    // Correct credentials
                    if (task.isSuccessful) {
                        // Proceed to change password
                        changePassword(
                            binding.etNewPass.text.toString().trim { it <= ' ' }
                        )
                    }
                    // Wrong credentials
                    else {
                        mUtility.hideProgressDialog()  // Hide the loading message

                        clearChangePassword()  // Clear the change password fields

                        // Display the error message
                        mUtility.showSnackBar(
                            requireActivity(),
                            task.exception!!.message.toString(),
                            true
                        )
                    }
                }  // end of reauthenticate
        }  // end of if

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
                        requireContext(), getString(R.string.msg_pwd_change_success)
                    )

                    // Sends the user back to the previous fragment
                    requireActivity().onBackPressed()
                }
                // If it is not successful
                else {
                    mUtility.hideProgressDialog()  // Hide the loading message

                    clearChangePassword()  // Clear the change password fields

                    // Display the error message
                    mUtility.showSnackBar(
                        requireActivity(), task.exception!!.message.toString(),
                        true
                    )
                }
            }  // end of updatePassword

    }  // end of changePassword method

    // Function to clear the change password text fields for security purposes
    private fun clearChangePassword() {
        with(binding) {
            if (etCurrentPass.text!!.isNotEmpty()) etCurrentPass.text!!.clear()
            if (etNewPass.text!!.isNotEmpty()) etNewPass.text!!.clear()
            if (etConfNewPass.text!!.isNotEmpty()) etConfNewPass.text!!.clear()
        }  // end of with(binding)
    }  // end of clearChangePassword method

}  // end of ChangePasswordFragment class
