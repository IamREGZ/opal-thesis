package edu.cccdci.opal.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    //For Cloud Firestore (Collection, Documents, and Fields)
    const val USERS: String = "users"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val GENDER: String = "gender"
    const val PHONENUM: String = "phoneNum"
    const val PROFILEPIC: String = "profilePic"

    //For storing information to Firestore
    const val GENDER_MALE: String = "male"
    const val GENDER_FEMALE: String = "female"
    const val GENDER_OTHER: String = "other"

    //For Shared Preferences and Parcelable
    const val OPAL_PREFERENCES: String = "OPALPrefs"
    const val SIGNED_IN_FULL_NAME: String = "signed_in_full_name"
    const val SIGNED_IN_USERNAME: String = "signed_in_username"
    const val SIGNED_IN_PROFILE_PIC: String = "signed_in_profile_pic"
    const val SIGNED_IN_USER_ROLE: String = "signed_in_user_role"
    const val EXTRA_USER_INFO: String = "extra_user_info"

    //For Requesting Permissions
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val SELECT_IMAGE_REQUEST_CODE = 1

    //File upload naming templates
    const val USER_PROFILE_IMAGE_TEMP: String = "OPAL_USRIMG_"

    //Function to launch the Image Selection Activity
    fun showImageSelection(activity: Activity) {

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        //Opens the Image Selection Activity
        activity.startActivityForResult(galleryIntent, SELECT_IMAGE_REQUEST_CODE)

    } //end of showImageSelection method

    /* Function to get the file extension of the file being uploaded
     * Example: C:\Users\username\Desktop\spider-man.jpg
     * The function will return a String value of ".jpg"
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? =
        MimeTypeMap.getSingleton().getExtensionFromMimeType(
            activity.contentResolver.getType(uri!!)
        )
}