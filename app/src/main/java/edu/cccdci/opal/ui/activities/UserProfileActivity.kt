package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityUserProfileBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass
import java.io.IOException

class UserProfileActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var mUserInfo: User
    private var mSelectedImageFileURI: Uri? = null
    private var mUserProfileImageURL: String = ""
    private var mUserHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbUserProfileActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                // Get data from the parcelable class
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)!!

                // Fill up the available fields
                with(mUserInfo) {
                    // Full Name
                    etProfileFname.setText(firstName)
                    etProfileLname.setText(lastName)

                    // Email (Disabled)
                    etProfileEmail.isEnabled = false
                    etProfileEmail.setText(emailAdd)

                    // Username (Disabled)
                    etProfileUsername.isEnabled = false
                    etProfileUsername.setText(userName)

                    etProfilePhone.setText(phoneNum)  // Phone Number

                    // Check one of the radio buttons depending on the selected gender
                    when (gender) {
                        Constants.GENDER_MALE -> rbProfileMale.isChecked = true
                        Constants.GENDER_FEMALE -> rbProfileFemale.isChecked = true
                        Constants.GENDER_OTHER -> rbProfileOther.isChecked = true
                    }

                    // Load the current profile picture
                    GlideLoader(this@UserProfileActivity)
                        .loadImage(profilePic, ivUserProfilePhoto)

                }  // end of with(userInfo)
            }  // end of if

            // Click event for User Profile Photo ImageView
            ivUserProfilePhoto.setOnClickListener(this@UserProfileActivity)
            // Click event for Save Changes Button
            btnSaveProfileInfo.setOnClickListener(this@UserProfileActivity)

        }  // end of with(binding)

    }  // end of onCreate method

    // Override the back function
    override fun onBackPressed() {
        storeUserInfoChanges()  // Stores modified information (if any)

        // If there are any changes to the user profile information
        if (mUserHashMap.isNotEmpty()) {
            showAlertDialog(
                this@UserProfileActivity,
                resources.getString(R.string.dialog_edit_user_title),
                resources.getString(R.string.dialog_edit_user_message),
                true,
                resources.getString(R.string.dialog_save),
                resources.getString(R.string.dialog_dont_save)
            )
        } else {
            super.onBackPressed()
        }
    }  // end of onBackPressed method

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

                    // If there are any changes made, save user info
                    if (mUserHashMap.isNotEmpty()) {
                        saveUserInfoChanges()
                    } else {
                        /* Exit the activity if there are no changes made.
                         * This is to prevent unnecessary reads and writes
                         * in Cloud Firestore.
                         */

                        // Displays the Toast message
                        toastMessage(
                            this@UserProfileActivity,
                            resources.getString(R.string.msg_no_user_info_changed)
                        )

                        finish()  // Closes the current activity
                    }  // end of if-else
                }

            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Function to check if storage permission is granted or denied
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
                    resources.getString(R.string.err_permission_denied),
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
                        mSelectedImageFileURI!!,
                        binding.ivUserProfilePhoto
                    )
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@UserProfileActivity,
                    resources.getString(R.string.err_image_selection_failed)
                )
            } // end of try-catch

        } // end of if

    } // end of onActivityResult method

    // Function to validate user profile information changes
    private fun validateProfChanges(): Boolean {
        with(binding) {
            return when {
                // If the First Name field is empty
                TextUtils.isEmpty(etProfileFname.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@UserProfileActivity,
                        resources.getString(R.string.err_blank_fname),
                        true
                    )
                    false  // return false
                }

                // If the Last Name field is empty
                TextUtils.isEmpty(etProfileLname.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@UserProfileActivity,
                        resources.getString(R.string.err_blank_lname),
                        true
                    )
                    false  // return false
                }

                // If the Phone Number field is empty
                TextUtils.isEmpty(etProfilePhone.text.toString().trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@UserProfileActivity,
                        resources.getString(R.string.err_blank_phone),
                        true
                    )
                    false  // return false
                }

                // If no gender is selected
                rgProfileGender.checkedRadioButtonId == -1 -> {
                    // Display an error message
                    showSnackBar(
                        this@UserProfileActivity,
                        resources.getString(R.string.err_no_gender_selected),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all inputs are valid
            }  // end of when

        }  // end of with(binding)

    }  // end of validateProfChanges method

    // Function to proceed with saving user information
    fun saveUserInfoChanges() {
        // Validate user inputs
        if (validateProfChanges()) {
            // Display the loading message (Saving Changes...)
            showProgressDialog(
                this@UserProfileActivity,
                this@UserProfileActivity,
                resources.getString(R.string.msg_saving_changes)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@UserProfileActivity, mSelectedImageFileURI
                )
            } else {
                // If the user didn't upload the image
                updateUserInfo()
            }  // end of if-else

        }  // end of if
    }  // end of saveUserInfoChanges method

    /* Function to prompt that the user has successfully uploaded the image.
     * And then updates the user information.
     */
    fun imageUploadSuccess(imageURL: String) {
        // Store the image URL of User Profile Picture
        mUserProfileImageURL = imageURL

        // Proceed to update the rest of the user information
        updateUserInfo()
    }  // end of imageUploadSuccess method

    // Function to store modified user profile information
    private fun storeUserInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mUserHashMap.clear()

        with(binding) {
            val firstName = etProfileFname.text.toString().trim { it <= ' ' }
            // Save the new first name if it is different from previous first name
            if (firstName != mUserInfo.firstName)
                mUserHashMap[Constants.FIRST_NAME] = firstName

            val lastName = etProfileLname.text.toString().trim { it <= ' ' }
            // Save the new last name if it is different from previous last name
            if (lastName != mUserInfo.lastName)
                mUserHashMap[Constants.LAST_NAME] = lastName

            val phoneNumber = etProfilePhone.text.toString().trim { it <= ' ' }
            // Save the new phone number if it is different from previous phone number
            if (phoneNumber != mUserInfo.phoneNum)
                mUserHashMap[Constants.PHONENUM] = phoneNumber

            // Stores gender value, depending on the selected radio button
            val gender = when {
                rbProfileMale.isChecked -> Constants.GENDER_MALE
                rbProfileFemale.isChecked -> Constants.GENDER_FEMALE
                rbProfileOther.isChecked -> Constants.GENDER_OTHER
                else -> ""
            }
            // Save the new gender if it is different from previous gender
            if (gender != mUserInfo.gender)
                mUserHashMap[Constants.GENDER] = gender

            // Check if a user has uploaded a new image, add a temporary value
            if (mSelectedImageFileURI != null)
                mUserHashMap[Constants.PROFILEPIC] = "0"
        }  // end of with(binding)
    }  // end of storeUserInfoChanges method

    // Function to store modified information to Firestore (if any)
    private fun updateUserInfo() {
        // Overwrite user profile image URL, if the user uploaded the image
        if (mUserProfileImageURL.isNotEmpty())
            mUserHashMap[Constants.PROFILEPIC] = mUserProfileImageURL

        // Proceed to update fields in the Cloud Firestore
        FirestoreClass().updateUserProfileData(
            this@UserProfileActivity, mUserHashMap
        )
    } // end of updateUserInfo method

    // Function to prompt that the user changes was made
    fun userInfoChangedPrompt() {
        hideProgressDialog() // Hide the loading message

        // Displays the Toast message
        toastMessage(
            this@UserProfileActivity,
            resources.getString(R.string.msg_user_info_changed)
        )

        finish()  // Closes the current activity
    }  // end of userInfoChangedPrompt()

}  // end of UserProfileActivity class
