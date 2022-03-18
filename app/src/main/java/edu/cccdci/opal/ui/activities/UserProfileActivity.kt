package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityUserProfileBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.*
import java.io.IOException

class UserProfileActivity : UtilityClass(), View.OnClickListener, View.OnLongClickListener {

    private lateinit var binding: ActivityUserProfileBinding
    private var mUserInfo: User? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mTempProfileImageURL: String = ""
    private var mUserHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbUserProfileActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                // Get data from the parcelable class
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)

                setUserProfileFields()  // Fill up the available fields
            }  // end of if

            // Click event for User Profile Photo ImageView
            ivUserProfilePhoto.setOnClickListener(this@UserProfileActivity)
            // Long click event for User Profile Photo ImageView
            ivUserProfilePhoto.setOnLongClickListener(this@UserProfileActivity)
            // Click event for Save Changes Button
            btnSaveProfileInfo.setOnClickListener(this@UserProfileActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Change the user profile photo
                R.id.iv_user_profile_photo -> {
                    // If storage permission access is already granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Launch the Image Selection
                        Constants.showImageSelection(this@UserProfileActivity)
                    } else {
                        // Request a storage access permission to the device
                        ActivityCompat.requestPermissions(
                            this@UserProfileActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }  // end of if-else
                }

                // Saves information changes made by user
                R.id.btn_save_profile_info -> {
                    // Stores modified information (if any)
                    storeUserInfoChanges()

                    if (mUserHashMap.isNotEmpty()) {
                        // If there are any changes made, save user info
                        saveUserInfoChanges()
                    } else {
                        /* Exit the activity if there are no changes made.
                         * This is to prevent unnecessary reads and writes
                         * in Cloud Firestore.
                         */

                        // Displays the Toast message
                        toastMessage(
                            this@UserProfileActivity,
                            getString(R.string.msg_no_user_info_changed)
                        )

                        finish()  // Closes the current activity
                    }  // end of if-else
                }

            }  // end of when
        }  // end of if

    }  // end of onClick method

    // onLongClick events are declared here
    override fun onLongClick(view: View?): Boolean {
        if (view != null) {
            when (view.id) {
                // Clear the user profile photo
                R.id.iv_user_profile_photo -> {
                    // Clear the profile photo if there's an uploaded image
                    if (mTempProfileImageURL.isNotEmpty()) {
                        /* Display an alert dialog with two action buttons
                         * (Remove & Cancel)
                         */
                        DialogClass(this@UserProfileActivity).alertDialog(
                            getString(R.string.dialog_profile_pic_remove_title),
                            getString(R.string.dialog_profile_pic_remove_message),
                            getString(R.string.dialog_btn_remove),
                            getString(R.string.dialog_btn_cancel)
                        )
                    } else {
                        // Display an error message
                        showSnackBar(
                            this@UserProfileActivity,
                            getString(R.string.err_no_profile_pic_to_remove),
                            true
                        )
                    }  // end of if-else
                }

            }  // end of when
        }  // end of if

        return true
    }  // end of onLongClick method

    // Override the back function
    override fun onBackPressed() {
        storeUserInfoChanges()  // Stores modified information (if any)

        // If there are any changes to the user profile information
        if (mUserHashMap.isNotEmpty()) {
            /* Display an alert dialog with three action buttons
             * (Save, Don't Save & Cancel)
             */
            DialogClass(this@UserProfileActivity).alertDialog(
                getString(R.string.dialog_edit_user_title),
                getString(R.string.dialog_edit_user_message),
                getString(R.string.dialog_btn_save),
                getString(R.string.dialog_btn_dont_save),
                getString(R.string.dialog_btn_cancel)
            )
        } else {
            super.onBackPressed()
        }
    }  // end of onBackPressed method

    // Function to check if storage permission is granted or denied
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If the user grants storage permission access
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Launch the Image Selection
                Constants.showImageSelection(this@UserProfileActivity)
            } else {
                // If the user denies the permission access
                showSnackBar(
                    this@UserProfileActivity,
                    getString(R.string.err_permission_denied),
                    true
                )
            }  // end of if-else

        }  // end of if

    }  // end of onRequestPermissionsResult method

    // Function to change image in the user profile ImageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.SELECT_IMAGE_REQUEST_CODE
            && data != null
        ) {
            try {
                // URI of selected image file
                mSelectedImageFileURI = data.data!!

                // Sets the ImageView to the selected image file
                GlideLoader(this@UserProfileActivity)
                    .loadImage(
                        mSelectedImageFileURI!!, binding.ivUserProfilePhoto
                    )

                // Set the temporary image URL to the URI of selected image
                mTempProfileImageURL = mSelectedImageFileURI.toString()

                // Make the "Long Press to Remove" instruction visible
                binding.tvDeleteProfilePicLabel.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@UserProfileActivity,
                    getString(R.string.err_image_selection_failed)
                )
            } // end of try-catch

        } // end of if

    } // end of onActivityResult method

    // Function to store user's profile information to the respective fields
    private fun setUserProfileFields() {
        with(binding) {
            mUserInfo?.let {
                // Full Name
                etProfileFirstName.setText(it.firstName)
                etProfileLastName.setText(it.lastName)

                etProfileEmail.setText(it.emailAdd)  // Email (Disabled)
                etProfileUsername.setText(it.userName)  // Username (Disabled)

                // Phone Number
                etProfilePhone.setText(it.phoneNum)

                // Check one of the radio buttons depending on the selected gender
                when (it.gender) {
                    Constants.GENDER_MALE -> rbProfileMale.isChecked = true
                    Constants.GENDER_FEMALE -> rbProfileFemale.isChecked = true
                    Constants.GENDER_OTHER -> rbProfileOther.isChecked = true
                }

                // Load the current profile picture
                GlideLoader(this@UserProfileActivity)
                    .loadImage(it.profilePic, ivUserProfilePhoto)

                // Store the temporary URL of user's profile picture
                mTempProfileImageURL = it.profilePic

                /* Make the "Long Press to Remove" instruction visible
                 * if there's any image uploaded
                 */
                if (mTempProfileImageURL.isNotEmpty())
                    tvDeleteProfilePicLabel.visibility = View.VISIBLE
            }  // end of let
        }  // end of with(binding)

    }  // end of setUserProfileFields method

    // Function to remove user's profile picture
    internal fun removeProfileImage() {
        // Set the profile photo to default placeholder
        GlideLoader(this@UserProfileActivity)
            .loadImage("", binding.ivUserProfilePhoto)

        // Clear all image selection values
        mTempProfileImageURL = ""
        mSelectedImageFileURI = null

        // Make the "Long Press to Remove" instruction not visible
        binding.tvDeleteProfilePicLabel.visibility = View.GONE

        // Display a success message
        showSnackBar(
            this@UserProfileActivity,
            getString(R.string.msg_profile_pic_removed),
            false
        )
    }  // end of removeProfileImage method

    // Function to validate user profile information changes
    private fun validateProfChanges(): Boolean {
        with(binding) {
            // Create a FormValidation object, and then execute the validations
            return FormValidation(this@UserProfileActivity).run {
                when {
                    !validateName(etProfileFirstName) -> false
                    !validateName(etProfileLastName) -> false
                    !validatePhoneNumber(etProfilePhone) -> false
                    !checkRadioSelection(rgProfileGender) -> false
                    else -> true
                }  // end of when
            }  // end of run

        }  // end of with(binding)

    }  // end of validateProfChanges method

    // Function to proceed with saving user information
    internal fun saveUserInfoChanges() {
        // Validate user inputs
        if (validateProfChanges()) {
            // Display the loading message (Saving Changes...)
            showProgressDialog(
                this@UserProfileActivity, this@UserProfileActivity,
                getString(R.string.msg_saving_changes)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@UserProfileActivity, mSelectedImageFileURI
                )
            }
            // If the user didn't upload the image
            else {
                // Just update the user information without profile image
                updateUserInfo()
            }  // end of if-else
        }  // end of if

    }  // end of saveUserInfoChanges method

    // Function to store modified user profile information
    private fun storeUserInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mUserHashMap.clear()

        with(binding) {
            mUserInfo?.let { user ->
                val firstName = etProfileFirstName.text.toString().trim { it <= ' ' }
                // Save the new first name if it is different from previous first name
                if (firstName != user.firstName)
                    mUserHashMap[Constants.FIRST_NAME] = firstName

                val lastName = etProfileLastName.text.toString().trim { it <= ' ' }
                // Save the new last name if it is different from previous last name
                if (lastName != user.lastName)
                    mUserHashMap[Constants.LAST_NAME] = lastName

                val phoneNumber = etProfilePhone.text.toString().trim { it <= ' ' }
                // Save the new phone number if it is different from previous phone number
                if (phoneNumber != user.phoneNum)
                    mUserHashMap[Constants.PHONE_NUM] = phoneNumber

                // Stores gender value, depending on the selected radio button
                val gender = when {
                    rbProfileMale.isChecked -> Constants.GENDER_MALE
                    rbProfileFemale.isChecked -> Constants.GENDER_FEMALE
                    rbProfileOther.isChecked -> Constants.GENDER_OTHER
                    else -> ""
                }
                // Save the new gender if it is different from previous gender
                if (gender != user.gender)
                    mUserHashMap[Constants.GENDER] = gender

                // Check if a user has changed the profile image, add a temporary value
                if (mTempProfileImageURL != user.profilePic)
                    mUserHashMap[Constants.PROFILE_PIC] = mTempProfileImageURL
            }  // end of let
        }  // end of with(binding)

    }  // end of storeUserInfoChanges method

    // Function to store modified information to Firestore
    internal fun updateUserInfo(imageURL: String? = null) {
        // Overwrite user profile image URL, if the user uploaded the new image
        if (imageURL != null)
            mUserHashMap[Constants.PROFILE_PIC] = imageURL

        // Proceed to update fields in the Cloud Firestore
        FirestoreClass().updateUserProfileData(
            this@UserProfileActivity, mUserHashMap
        )
    } // end of updateUserInfo method

    // Function to prompt that the user changes was made
    internal fun userInfoChangedPrompt() {
        hideProgressDialog() // Hide the loading message

        // Displays the Toast message
        toastMessage(
            this@UserProfileActivity,
            getString(R.string.msg_user_info_changed)
        )

        finish()  // Closes the current activity
    }  // end of userInfoChangedPrompt()

}  // end of UserProfileActivity class
