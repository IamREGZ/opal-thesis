package edu.cccdci.opal.utils

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore

object Constants {
    const val USERS: String = "users"
    const val OPAL_PREFERENCES: String = "OPALPrefs"
    const val SIGNED_IN_FULL_NAME: String = "signed_in_full_name"
    const val SIGNED_IN_USERNAME: String = "signed_in_username"
    const val EXTRA_USER_INFO: String = "extra_user_info"
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val SELECT_IMAGE_REQUEST_CODE = 1

    //Function to launch the Image Selection Activity
    fun showImageSelection(activity: Activity) {

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        //Opens the Image Selection Activity
        activity.startActivityForResult(galleryIntent, SELECT_IMAGE_REQUEST_CODE)

    } //end of showImageSelection method
}