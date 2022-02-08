package edu.cccdci.opal.utils

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.google.android.material.button.MaterialButton
import edu.cccdci.opal.R
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    // For Cloud Firestore (Collection, Documents, and Fields)
    const val USERS: String = "users"
    const val ID: String = "id"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val CUSTOMER_USERNAME: String = "custUser"
    const val GENDER: String = "gender"
    const val PHONENUM: String = "phoneNum"
    const val PROFILEPIC: String = "profilePic"
    const val VENDOR: String = "vendor"
    const val ADDRESSES: String = "addresses"
    const val FULL_NAME: String = "fullName"
    const val PROVINCE: String = "province"
    const val CITY: String = "city"
    const val BARANGAY: String = "barangay"
    const val POSTAL: String = "postal"
    const val DETAIL_ADDR: String = "detailAdd"
    const val DEFAULT_ADDR: String = "default"
    const val NAME: String = "name"
    const val IMAGE: String = "image"
    const val DESCRIPTION: String = "description"
    const val PRICE: String = "price"
    const val UNIT: String = "unit"
    const val WEIGHT: String = "weight"
    const val STOCK: String = "stock"
    const val STATUS: String = "status"
    const val VENDOR_ID: String = "vendorID"
    const val CART: String = "cart"
    const val CART_ITEMS: String = "cartItems"
    const val DATES: String = "dates"
    const val ORDER_DATE: String = "orderDate"
    const val PAYMENT_DATE: String = "paymentDate"
    const val DELIVER_DATE: String = "deliverDate"
    const val RETURN_DATE: String = "returnDate"
    const val ORDER_PAYMENT: String = "payment"
    const val CUSTOMER_ID: String = "customerID"
    const val ADDRESS: String = "address"
    const val ORDER_ITEMS: String = "orderItems"
    const val TOTAL_PRICE: String = "totalPrice"
    const val SUB_TOTAL: String = "subtotal"
    const val DELIVERY_FEE_PRICE: String = "deliveryFee"
    const val ORDER_ACTION: String = "orderAction"
    const val SPECIAL_INSTRUCTIONS: String = "special"
    const val WET_MARKET: String = "wetMarket"
    const val CATEGORY: String = "category"
    const val OTHER_CATEGORY: String = "otherCat"

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

    // Cloud Firestore constants for Markets
    const val MARKETS: String = "markets"
    const val MARKET_ID: String = "marketID"
    const val MARKET_NAME: String = "marketName"

    // Cloud Firestore constants for Orders
    const val CUSTOMER_ORDERS: String = "orders"

    // Payment Information constants
    const val PAYMENT_METHOD: String = "payment_method"
    const val IS_SELECTED: String = "is_selected"

    // For storing information to Firestore
    const val GENDER_MALE: String = "male"
    const val GENDER_FEMALE: String = "female"
    const val GENDER_OTHER: String = "other"
    const val ADDRESS_ID_TEMP: String = "ADD-"
    const val PRODUCT_ID_TEMP: String = "PD-"
    const val MARKET_ID_TEMP: String = "MK-"
    const val ORDER_ID_TEMP: String = "OPL"
    const val ITEM_OTHERS: String = "Others"

    // Product Status Codes
    const val PRODUCT_IN_STOCK: Int = 1
    const val PRODUCT_VIOLATION: Int = 2
    const val PRODUCT_UNLISTED: Int = 0

    // Order Status Codes
    const val ORDER_PENDING_CODE: Int = 0
    const val ORDER_TO_DELIVER_CODE: Int = 1
    const val ORDER_OFD_CODE: Int = 2
    const val ORDER_DELIVERED_CODE: Int = 3
    const val ORDER_CANCELLED_CODE: Int = 4
    const val ORDER_RETURN_REQUEST_CODE: Int = 5
    const val ORDER_TO_RETURN_CODE: Int = 6
    const val ORDER_RETURNED_CODE: Int = 7

    // For Shared Preferences and Parcelable
    const val OPAL_PREFERENCES: String = "OPALPrefs"
    const val SIGNED_IN_FULL_NAME: String = "signed_in_full_name"
    const val SIGNED_IN_USERNAME: String = "signed_in_username"
    const val SIGNED_IN_PROFILE_PIC: String = "signed_in_profile_pic"
    const val SIGNED_IN_USER_ROLE: String = "signed_in_user_role"
    const val EXTRA_USER_INFO: String = "extra_user_info"
    const val PRODUCT_DESCRIPTION: String = "product_description"
    const val USER_ADDRESS: String = "user_address"
    const val SELECTED_ADDRESS: String = "selected_address"
    const val MARKET_INFO: String = "market_info"
    const val CART_PRODUCT_DETAILS: String = "cart_product_details"
    const val SELECTABLE_ENABLED: String = "selectable_enabled"
    const val SELECTED_PAYMENT_METHOD: String = "selected_payment_method"
    const val ORDER_DETAILS: String = "order_details"
    const val IS_VENDOR: String = "is_vendor"
    const val MARKET_ID_DATA: String = "market_id"

    // Request Permission Codes
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val SELECT_IMAGE_REQUEST_CODE = 1

    // File upload naming templates
    const val USER_PROFILE_IMAGE_TEMP: String = "OPAL_USRIMG_"
    const val PRODUCT_IMAGE_TEMP: String = "OPAL_PDT_"
    const val MARKET_IMAGE_TEMP: String = "OPAL_MKT_"

    // Date formatting constants
    const val MDY_HM12_DATE_FORMAT: String = "MM/dd/yyyy hh:mm a"
    const val YMD_HMS24_DATE_FORMAT: String = "yyyy-MM-dd HH:mm:ss"

    // Hex Color constants
    const val APP_DARK_GREEN: String = "#FF014421"
    const val APP_DARK_TEAL: String = "#FF006666"
    const val APP_BLACK: String = "#FF0F0F0F"
    const val MEDIUM_ORANGE: String = "#FFF28500"
    const val DIM_GRAY: String = "#FF696969"

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

    // Function to get the formatted date according to the pattern
    fun formatDate(pattern: String, date: Date): String {
        return SimpleDateFormat(pattern, Locale.ENGLISH).format(date)
    }  // end of formatDate method

    /* Function to change the appearance of button to secondary style (green
     * outline & text)
     */
    fun toSecondaryButton(button: MaterialButton) {
        // Object to toggle colors depending on the button's state
        val cslBtn2 = ColorStateList(
            // Array of states: Enabled and Disabled
            arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled)
            ),
            // Enabled = Dark Green; Disabled = Dim Gray
            intArrayOf(
                Color.parseColor(APP_DARK_GREEN), Color.parseColor(DIM_GRAY)
            )
        )

        // Change the button's background
        button.setBackgroundResource(R.drawable.button_secondary)
        // Change the button's text color
        button.setTextColor(cslBtn2)
        // Change the button's icon tint
        button.iconTint = cslBtn2
    }  // end of toSecondaryButton method

}  // end of Constants object