package edu.cccdci.opal.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    // For Cloud Firestore (Collection, Documents, and Fields)
    const val USERS: String = "users"
    const val ID: String = "id"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val GENDER: String = "gender"
    const val PHONENUM: String = "phoneNum"
    const val PROFILEPIC: String = "profilePic"
    const val VENDOR: String = "vendor"
    const val ADDRESSES: String = "addresses"
    const val ADDRESS_ID_TEMP: String = "ADD-"
    const val FULL_NAME: String = "fullName"
    const val PROVINCE: String = "province"
    const val CITY: String = "city"
    const val BARANGAY: String = "barangay"
    const val POSTAL: String = "postal"
    const val DETAIL_ADDR: String = "detailAdd"
    const val DEFAULT_ADDR: String = "default"
    const val PICKUP_ADDR: String = "pickup"
    const val NAME: String = "name"
    const val IMAGE: String = "image"
    const val DESCRIPTION: String = "description"
    const val PRICE: String = "price"
    const val UNIT: String = "unit"
    const val WEIGHT: String = "weight"
    const val STOCK: String = "stock"
    const val STATUS: String = "status"
    const val CART: String = "cart"
    const val CART_ITEMS: String = "cartItems"

    // Cloud Firestore constants for Addresses
    const val PRV_DOC: String = "DOC-PRV"
    const val PROVINCES: String = "provinces"
    const val PROVINCE_ID: String = "provinceID"
    const val PROVINCE_NAME: String = "provinceName"
    const val CT_DOC: String = "DOC-CT"
    const val CITY_COL: String = "cities"
    const val CITY_ID: String = "cityID"
    const val CITY_NAME: String = "cityName"

    // Cloud Firestore constants for Products
    const val PRODUCTS: String = "products"
    const val PRODUCT_ID_TEMP: String = "PD-"
    const val PRODUCT_VENDOR_ID: String = "vendorID"
    const val PRODUCT_UNIT_OTHERS: String = "Others"
    const val PRODUCT_IN_STOCK: Int = 1
    const val PRODUCT_VIOLATION: Int = 2
    const val PRODUCT_UNLISTED: Int = 0

    // Cloud Firestore constants for Markets
    const val MARKETS: String = "markets"
    const val MARKET_ID: String = "marketID"

    // For storing information to Firestore
    const val GENDER_MALE: String = "male"
    const val GENDER_FEMALE: String = "female"
    const val GENDER_OTHER: String = "other"

    // For Shared Preferences and Parcelable
    const val OPAL_PREFERENCES: String = "OPALPrefs"
    const val SIGNED_IN_FULL_NAME: String = "signed_in_full_name"
    const val SIGNED_IN_USERNAME: String = "signed_in_username"
    const val SIGNED_IN_PROFILE_PIC: String = "signed_in_profile_pic"
    const val SIGNED_IN_USER_ROLE: String = "signed_in_user_role"
    const val EXTRA_USER_INFO: String = "extra_user_info"
    const val PRODUCT_DESCRIPTION: String = "product_description"
    const val USER_ADDRESS: String = "user_address"
    const val SELECTABLE_ADDRESS: String = "selectable_address"

    // Request Permission Codes
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val SELECT_IMAGE_REQUEST_CODE = 1
    const val SELECT_ADDRESS_REQUEST_CODE = 3

    // File upload naming templates
    const val USER_PROFILE_IMAGE_TEMP: String = "OPAL_USRIMG_"
    const val PRODUCT_IMAGE_TEMP: String = "OPAL_PDT_"

    // Function to launch the Image Selection Activity
    fun showImageSelection(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Opens the Image Selection Activity
        activity.startActivityForResult(galleryIntent, SELECT_IMAGE_REQUEST_CODE)
    }  // end of showImageSelection method

    /* Function to get the file extension of the file being uploaded
     * Example: C:\Users\username\Desktop\spider-man.jpg
     * The function will return a String value of ".jpg"
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(
            activity.contentResolver.getType(uri!!)
        )
    }  // end of getFileExtension method

}  // end of Constants object