package edu.cccdci.opal.utils

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.cccdci.opal.ui.activities.AddressEditActivity
import edu.cccdci.opal.ui.activities.MainActivity
import edu.cccdci.opal.ui.activities.UserProfileActivity
import edu.cccdci.opal.ui.fragments.DeleteAccountFragment

/**
 * A class containing dialog popups.
 */
class DialogClass(
    private val mContext: Context,
    private val mFragment: Fragment? = null
) {

    // Function to display an alert dialog with one action button
    fun alertDialog(
        title: String, message: String, positive: String
    ) {
        // Create an Alert Dialog Builder object
        MaterialAlertDialogBuilder(mContext).apply {
            // Set the title of alert dialog
            setTitle(title)
            // Set the message of alert dialog
            setMessage(message)
            // Set the positive action button functionality
            setPositiveButton(positive) { dialog, _ ->
                dialog.dismiss()
            }
            // Ensures that the dialog will not disappear when clicked outside
            setCancelable(false)
            // Finally, display the alert dialog
            show()
        }  // end of apply

    }  // end of alertDialog method

    // Function to display an alert dialog with two action buttons
    fun alertDialog(
        title: String, message: String, positive: String, negative: String,
        actionID: Int? = null
    ) {
        // Create an Alert Dialog Builder
        MaterialAlertDialogBuilder(mContext).apply {
            // Set the title of alert dialog
            setTitle(title)
            // Set the message of alert dialog
            setMessage(message)
            // Set the negative action button functionality (left)
            setNegativeButton(negative) { dialog, _ ->
                // Fragments only
                if (mFragment != null) {
                    when (mFragment) {
                        // Clear the password field in Delete Account Fragment (Cancel)
                        is DeleteAccountFragment -> mFragment.clearPassword()
                    }
                }

                dialog.dismiss()
            }
            // Set the positive action button functionality (right)
            setPositiveButton(positive) { dialog, _ ->
                // Fragments only
                if (mFragment != null) {
                    when (mFragment) {
                        // Proceed with account deletion in Delete Account Fragment (Delete)
                        is DeleteAccountFragment -> mFragment.verifyCredentials()
                    }
                }
                // Activities only
                else {
                    when (mContext) {
                        // Log out the current user in Main Activity (Log Out)
                        is MainActivity -> mContext.logOutUser()
                        // Remove the profile image in User Profile Activity (Remove)
                        is UserProfileActivity -> mContext.removeProfileImage()
                        // Two possible actions for Address Edit Activity
                        is AddressEditActivity -> {
                            if (actionID != null) {
                                when (actionID) {
                                    // Delete the address in Address Edit Activity (Delete)
                                    Constants.DELETE_ADDRESS_ACTION -> mContext
                                        .deleteUserAddress()
                                    // Exit the activity in Address Edit Activity (Exit)
                                    Constants.EXIT_ADDRESS_ACTION -> mContext.finish()
                                }
                            }
                        }
                    }
                }
                dialog.dismiss()
            }
            // Ensures that the dialog will not disappear when clicked outside
            setCancelable(false)
            // Finally, display the alert dialog
            show()
        }  // end of apply

    }  // end of alertDialog method

    // Function to display an alert dialog with three action buttons
    fun alertDialog(
        title: String, message: String, positive: String,
        negative: String, neutral: String, actionID: Int? = null
    ) {
        // Create an Alert Dialog Builder object
        MaterialAlertDialogBuilder(mContext).apply {
            // Set the title of alert dialog
            setTitle(title)
            // Set the message of alert dialog
            setMessage(message)
            // Set the neutral action button functionality (left)
            setNeutralButton(neutral) { dialog, _ ->
                dialog.dismiss()
            }
            // Set the negative action button functionality (middle)
            setNegativeButton(negative) { dialog, _ ->
                when (mContext) {
                    // Close the User Profile Activity (Don't Save)
                    is UserProfileActivity -> mContext.finish()
                    // Close the Address Edit Activity (Don't Save)
                    is AddressEditActivity -> mContext.finish()
                }
                dialog.dismiss()
            }
            // Set the positive action button functionality (right)
            setPositiveButton(positive) { dialog, _ ->
                when (mContext) {
                    // Save user profile info changes in User Profile Activity (Save)
                    is UserProfileActivity -> mContext.saveUserInfoChanges()
                    // Save address info changes in Address Edit Activity (Save)
                    is AddressEditActivity -> mContext.saveAddressChanges()
                }
                dialog.dismiss()
            }
            // Ensures that the dialog will not disappear when clicked outside
            setCancelable(false)
            // Finally, display the dialog
            show()
        }  // end of apply

    }  // end of alertDialog method

}  // end of DialogClass
