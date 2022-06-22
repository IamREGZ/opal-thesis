package edu.cccdci.opal.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.adapters.ProductInventoryAdapter
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.ui.activities.*
import edu.cccdci.opal.ui.fragments.*

/**
 * A class containing dialog popups.
 */
class DialogClass(
    private val mContext: Context,
    private val mFragment: Fragment? = null,
    private val mObj: Any? = null,
    private val mAdapter: Any? = null
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
                        is HomeFragment -> {
                            mContext.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        }

                        // Proceed with account deletion in Delete Account Fragment (Delete)
                        is DeleteAccountFragment -> mFragment.verifyCredentials()

                        // Three possible actions for all tabs in the Product Activity
                        is ProductInStockFragment,
                        is ProductSoldOutFragment,
                        is ProductViolationFragment,
                        is ProductUnlistedFragment -> {
                            if (actionID != null) {
                                when (actionID) {
                                    // Delete the product in Product Activity (Delete)
                                    Constants.DELETE_PRODUCT_ACTION -> {
                                        (mObj as? Product)?.let {
                                            (mAdapter as ProductInventoryAdapter)
                                                .deleteProductInfo(it)
                                        }
                                    }

                                    // Unlist the product in Product Activity (Unlist)
                                    Constants.UNLIST_PRODUCT_ACTION -> {
                                        (mObj as? Product)?.let {
                                            (mAdapter as ProductInventoryAdapter)
                                                .updateProductStatus(
                                                    it, Constants.PRODUCT_UNLISTED
                                                )
                                        }
                                    }

                                    // Relist the product in Product Activity (Relist)
                                    Constants.RELIST_PRODUCT_ACTION -> {
                                        (mObj as? Product)?.let {
                                            (mAdapter as ProductInventoryAdapter)
                                                .updateProductStatus(
                                                    it, Constants.PRODUCT_IN_STOCK
                                                )
                                        }
                                    }

                                }  // end of when
                            }  // end of if
                        }

                    }  // end of when
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
                                }  // end of when
                            }  // end of if
                        }

                        // Two possible actions for Market Edit Activity
                        is MarketEditorActivity -> {
                            if (actionID != null) {
                                when (actionID) {
                                    // Delete the image in Market Editor Activity (Delete)
                                    Constants.DELETE_MARKET_IMAGE_ACTION -> mContext
                                        .removeMarketImage()
                                    // Exit the activity in Market Editor Activity (Exit)
                                    Constants.EXIT_VENDOR_REG_ACTION -> mContext.finish()
                                }  // end of when
                            }  // end of if
                        }

                        // Two possible actions for Product Editor Activity
                        is ProductEditorActivity -> {
                            if (actionID != null) {
                                when (actionID) {
                                    // Delete the image in Product Editor Activity (Delete)
                                    Constants.DELETE_PRODUCT_IMAGE_ACTION -> mContext
                                        .removeProductImage()
                                    // Exit the activity in Product Editor Activity (Exit)
                                    Constants.EXIT_PRODUCT_ACTION -> mContext.finish()
                                }  // end of when
                            }  // end of if
                        }

                        // Remove the item(s) from the cart in Cart Activity (Remove)
                        is CartActivity -> (mAdapter as? CartAdapter)?.removeItemsFromCart(
                            mObj as? Int
                        )

                    }

                    dialog.dismiss()
                }  // end of if-else
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
                    // Close the Market Editor Activity (Don't Save)
                    is MarketEditorActivity -> mContext.finish()
                    // Close the Product Editor Activity (Don't Save)
                    is ProductEditorActivity -> mContext.finish()
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
                    // Save market info changes in Market Editor Activity (Save)
                    is MarketEditorActivity -> mContext.saveMarketChanges()
                    // Save product info changes in Product Editor Activity (Save)
                    is ProductEditorActivity -> mContext.saveProductChanges()
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
