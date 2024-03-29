package edu.cccdci.opal.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.LocationData
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.dataclasses.UserAddress
import edu.cccdci.opal.ui.fragments.HomeFragment
import org.json.JSONException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * An object to store constant variables and functions for reusability.
 */
object Constants {
    // Generic Cloud Firestore constants (Collection, Documents, and Fields)
    const val ID: String = "id"
    const val NAME: String = "name"
    const val IMAGE: String = "image"
    const val DESCRIPTION: String = "description"
    const val STATUS: String = "status"
    const val VENDOR_ID: String = "vendorID"

    // Cloud Firestore constants for Users
    const val USERS: String = "users"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val GENDER: String = "gender"
    const val PHONE_NUM: String = "phoneNum"
    const val PROFILE_PIC: String = "profilePic"
    const val VENDOR: String = "vendor"
    const val CART: String = "cart"
    const val CART_ITEMS: String = "cartItems"
    const val LOCATION_SETTINGS: String = "locSettings"

    // Cloud Firestore constants for Addresses
    const val ADDRESSES: String = "addresses"
    const val FULL_NAME: String = "fullName"
    const val PROVINCE: String = "province"
    const val CITY: String = "city"
    const val BARANGAY: String = "barangay"
    const val POSTAL: String = "postal"
    const val DETAIL_ADDR: String = "detailAdd"
    const val DEFAULT_ADDR: String = "default"
    const val LOCATION: String = "location"
    const val GEO_HASH: String = "geoHash"

    // Cloud Firestore constants for Address Data
    const val PRV_DOC: String = "DOC-PRV"
    const val PROVINCES: String = "provinces"
    const val PROVINCE_ID: String = "provinceID"
    const val PROVINCE_NAME: String = "provinceName"
    const val CT_DOC: String = "DOC-CT"
    const val CITY_COL: String = "cities"
    const val CITY_ID: String = "cityID"
    const val CITY_NAME: String = "cityName"

    // Cloud Firestore constants for Markets
    const val MARKETS: String = "markets"
    const val MARKET_ID: String = "marketID"
    const val MARKET_NAME: String = "marketName"
    const val WET_MARKET: String = "wetMarket"
    const val CATEGORY: String = "category"
    const val OTHER_CATEGORY: String = "otherCat"

    // Cloud Firestore constants for Products
    const val PRODUCTS: String = "products"
    const val PRICE: String = "price"
    const val UNIT: String = "unit"
    const val WEIGHT: String = "weight"
    const val STOCK: String = "stock"

    // Cloud Firestore constants for Orders
    const val CUSTOMER_ORDERS: String = "orders"
    const val DATES: String = "dates"
    const val ORDER_DATE: String = "orderDate"
    const val PAYMENT_DATE: String = "paymentDate"
    const val DELIVER_DATE: String = "deliverDate"
    const val RETURN_DATE: String = "returnDate"
    const val ORDER_PAYMENT: String = "payment"
    const val CUSTOMER_ID: String = "customerID"
    const val CUSTOMER_USERNAME: String = "custUser"
    const val ADDRESS: String = "address"
    const val ORDER_ITEMS: String = "orderItems"
    const val TOTAL_PRICE: String = "totalPrice"
    const val SUB_TOTAL: String = "subtotal"
    const val DELIVERY_FEE_PRICE: String = "deliveryFee"
    const val ORDER_ACTION: String = "orderAction"
    const val SPECIAL_INSTRUCTIONS: String = "special"

    // Payment Information constants
    const val PAYMENT_METHOD: String = "payment_method"
    const val IS_SELECTED: String = "is_selected"

    // Generic Firestore Values
    const val ITEM_OTHERS: String = "Others"

    // Firestore Document ID Templates
    const val ADDRESS_ID_TEMP: String = "ADD-"
    const val PRODUCT_ID_TEMP: String = "PD-"
    const val MARKET_ID_TEMP: String = "MK-"
    const val ORDER_ID_TEMP: String = "OPL"

    // File upload naming templates
    const val USER_PROFILE_IMAGE_TEMP: String = "OPAL_USRIMG_"
    const val PRODUCT_IMAGE_TEMP: String = "OPAL_PDT_"
    const val MARKET_IMAGE_TEMP: String = "OPAL_MKT_"

    // User Genders
    const val GENDER_MALE: String = "male"
    const val GENDER_FEMALE: String = "female"
    const val GENDER_OTHER: String = "other"

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

    // Market Browse/Category Codes
    const val BROWSE_NEAR_ME: Int = 0
    const val BROWSE_BAGSAK: Int = 1
    const val BROWSE_SUKI: Int = 2
    const val BROWSE_RECENT: Int = 3
    const val CATEGORY_MEAT: Int = 0
    const val CATEGORY_SEAFOOD: Int = 1
    const val CATEGORY_POULTRY: Int = 2
    const val CATEGORY_FRUITS: Int = 3
    const val CATEGORY_VEGETABLES: Int = 4
    const val CATEGORY_RICE: Int = 5
    const val CATEGORY_OTHERS: Int = 6

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
    const val MARKET_NAME_DATA: String = "market_name_data"
    const val LOCATION_MARKERS_INFO: String = "location_markers_info"
    const val CURRENT_MARKER_POS: String = "current_marker_position"
    const val CURRENT_LOCATION: String = "current_location"
    const val SELECTION_MODE: String = "selection_mode"
    const val CATEGORY_CODE: String = "category_code"
    const val CATEGORY_TITLE: String = "category_title"
    const val CATEGORY_DESC: String = "category_desc"

    // Request Permission Codes
    const val READ_STORAGE_PERMISSION_CODE: Int = 2
    const val SELECT_IMAGE_REQUEST_CODE: Int = 1

    // Current Location Options Codes
    const val FROM_DEVICE_LOCATION_CODE: Int = 0
    const val FROM_USER_ADDRESS_CODE: Int = 1

    // Location Request Constants
    const val LOC_REQ_INTERVAL: Long = 600000L  // 10 mins
    const val LOC_REQ_FASTEST_INTERVAL: Long = 300000L  // 5 mins
    const val LOC_REQ_MAX_WAIT_TIME: Long = 1800000L  // 30 mins
    const val LOC_REQ_EXP_DURATION: Long = 120000L  // 20 mins

    // Address Selection Mode Codes
    const val SELECT_CHECKOUT_ADDRESS: Int = 0
    const val SELECT_CURRENT_LOCATION: Int = 1

    // Dialog Action IDs
    const val DELETE_ADDRESS_ACTION: Int = 10
    const val EXIT_ADDRESS_ACTION: Int = 11
    const val DELETE_MARKET_IMAGE_ACTION: Int = 20
    const val EXIT_VENDOR_REG_ACTION: Int = 21
    const val DELETE_PRODUCT_ACTION: Int = 30
    const val UNLIST_PRODUCT_ACTION: Int = 31
    const val RELIST_PRODUCT_ACTION: Int = 32
    const val DELETE_PRODUCT_IMAGE_ACTION: Int = 33
    const val EXIT_PRODUCT_ACTION: Int = 34

    // Regex patterns
    private const val VALID_EMAIL: String = "^[A-Za-z0-9]+(?:[._-]?[A-Za-z0-9])+@" +
            "[A-Za-z0-9]+(?:[._-]?[A-Za-z0-9])+(?:\\.[A-Za-z0-9]{2,3})+\$"
    private const val VALID_USERNAME: String = "^[A-Za-z0-9]+(?:[.-]?[A-Za-z0-9]+)*\$"
    private const val VALID_PHONE: String = "^(?:0|\\+63)9\\d{9}\$"
    private const val VALID_NUMERIC: String = "^-?\\d+(?:\\.\\d+)?\$"
    const val HAS_LOWERCASE: String = ".*[a-z].*"
    const val HAS_UPPERCASE: String = ".*[A-Z].*"
    const val HAS_DIGIT: String = ".*\\d.*"
    const val HAS_SPECIAL_CHAR: String = ".*[@%+'\"!#\$^?,(){}\\[\\]~`\\-_./\\\\].*"

    // Date formatting constants
    const val MDY_HM12_DATE_FORMAT: String = "MM/dd/yyyy hh:mm a"
    const val YMD_HMS24_DATE_FORMAT: String = "yyyy-MM-dd HH:mm:ss"

    // Hex Color constants
    const val APP_GREEN: String = "#FF00611C"
    const val APP_TEAL: String = "#FF00827F"
    const val APP_DARK_GREEN: String = "#FF014421"
    const val APP_DARK_TEAL: String = "#FF006666"
    const val APP_BLACK: String = "#FF0F0F0F"
    const val MEDIUM_ORANGE: String = "#FFF28500"
    const val DIM_GRAY: String = "#FF696969"

    // Default latitude and longitude (Jose Rizal's house)
    const val DEFAULT_LATITUDE: Double = 14.2136451
    const val DEFAULT_LONGITUDE: Double = 121.1667075

    // Google Maps API-related constants
    const val ZOOM_SMALL: Float = 18F
    const val ZOOM_LARGE: Float = 17F
    const val MAX_RADIUS_IN_M: Double = 5000.0

    // Google Distance Matrix API constants
    const val DISTANCE_MATRIX: String = "distancematrix"
    const val GET_REQUEST_METHOD: String = "GET"
    const val ROWS: String = "rows"
    const val ELEMENTS: String = "elements"
    const val DISTANCE: String = "distance"
    const val DURATION: String = "duration"
    const val VALUE: String = "value"

    // Google Directions API constants
    const val DIRECTIONS: String = "directions"
    const val ROUTES: String = "routes"
    const val LEGS: String = "legs"
    const val STEPS: String = "steps"
    const val POLYLINE: String = "polyline"
    const val POINTS: String = "points"

    // REGULAR EXPRESSION (REGEX) FUNCTIONS
    // Function to validate email string
    internal fun emailValidator(email: String): Boolean = Regex(VALID_EMAIL)
        .matches(email)

    // Function to validate username string
    internal fun usernameValidator(user: String): Boolean = Regex(VALID_USERNAME)
        .matches(user)

    // Function to validate phone number string
    internal fun phoneNumberValidator(phone: String): Boolean = Regex(VALID_PHONE)
        .matches(phone)

    // Function to validate number string
    internal fun isNumeric(number: String): Boolean = Regex(VALID_NUMERIC).matches(number)

    // Function to determine if the password meets the requirement for strong password
    internal fun strongPassword(pass: String): Boolean {
        /* Aside from having at least 8 characters, it must have:
         * 1. At least one lowercase letter,
         * 2. At least one uppercase letter,
         * 3. At least one digit, and
         * 4. At least one special character
         */
        return pass.run {
            matches(HAS_LOWERCASE.toRegex()) && matches(HAS_UPPERCASE.toRegex()) &&
                    matches(HAS_DIGIT.toRegex()) && matches(HAS_SPECIAL_CHAR.toRegex())
        }  // end of run
    }  // end of strongPassword method

    // Function to launch the Image Selection Activity
    internal fun showImageSelection(activity: Activity) {
        activity.startActivityForResult(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            SELECT_IMAGE_REQUEST_CODE
        )
    }  // end of showImageSelection method

    /* Function to get the file extension of the file being uploaded
     * Example: C:\Users\username\Desktop\spider-man.jpg
     * The function will return a String value of ".jpg"
     */
    internal fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(
            activity.contentResolver.getType(uri!!)
        )
    }  // end of getFileExtension method

    // Function to get the device's location with address information
    @SuppressLint("MissingPermission")
    internal fun getDeviceLocation(
        activity: Activity, location: Location?, fragment: Fragment? = null
    ) {
        var error: String? = null  // Error message (if available)
        var result: String? = null  // Resulting location data

        // If the retrieved location exists
        if (location != null) {
            try {
                // Use Geocoder to parse location data into address information
                val address = Geocoder(activity, Locale.getDefault())
                    // Get the latitude and longitude, with one result
                    .getFromLocation(
                        location.latitude, location.longitude, 1
                    )

                if (fragment != null) {
                    result = when (fragment) {
                        is HomeFragment -> Gson().toJson(
                            CurrentLocation(
                                FROM_DEVICE_LOCATION_CODE,
                                address[0].latitude,
                                address[0].longitude,
                                address[0].getAddressLine(0)
                            )
                        )

                        else -> null
                    }
                }
            } catch (e: IOException) {
                // Log the error and store its message
                e.printStackTrace()
                error = e.message
            } catch (e: JSONException) {
                // Log the error and store its message
                e.printStackTrace()
                error = e.message
            } catch (e: NullPointerException) {
                // Log the error and store its message
                e.printStackTrace()
                error = e.message
            } finally {
                // If there's an error, display a Toast message
                if (!error.isNullOrEmpty()) {
                    Toast.makeText(
                        activity,
                        activity.getString(
                            R.string.err_get_location_failed, error
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
                /* If none, execute the rest of the codes, depending on the
                 * activity or fragment.
                 */
                else {
                    // Prevents NPE
                    if (fragment != null) {
                        when (fragment) {
                            is HomeFragment -> fragment.storeLocationResult(result)
                        }
                    }
                }
            }  // end of try-catch-finally
        } else {
            // Display a Toast message if location is null
            Toast.makeText(
                activity,
                activity.getString(R.string.err_no_device_location_result),
                Toast.LENGTH_SHORT
            ).show()
        }  // end of if-else

    }  // end of getDeviceLocation method

    // Function to get the formatted date according to the pattern
    internal fun formatDate(pattern: String, date: Date): String {
        return SimpleDateFormat(pattern, Locale.ENGLISH).format(date)
    }  // end of formatDate method

    /* Function to change the appearance of button to secondary style (green
     * outline & text)
     */
    internal fun toSecondaryButton(button: MaterialButton) {
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

    // Function to return a Google Maps API URL
    internal fun getMapsURL(
        context: Context, origin: List<Double>, destination: List<Double>,
        apiName: String
    ): String {
        // Variable to store the API Key
        var apiKey: String? = null

        try {
            context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            ).apply {
                // Get the API key from Android Manifest
                apiKey = metaData.getString("com.google.android.geo.API_KEY") ?: ""
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()  // Log the exception
        } catch (e: NullPointerException) {
            e.printStackTrace()  // Log the exception
        }  // end of try-catch

        // 0 - Latitude; 1 - Longitude
        return if (apiName == DIRECTIONS) {
            "https://maps.googleapis.com/maps/api/${apiName}/json?origin=" +
                    "${origin[0]},${origin[1]}&destination=" +
                    "${destination[0]},${destination[1]}&key=${apiKey ?: ""}"
        } else {
            "https://maps.googleapis.com/maps/api/${apiName}/json?origins=" +
                    "${origin[0]},${origin[1]}&destinations=" +
                    "${destination[0]},${destination[1]}&key=${apiKey ?: ""}"
        }
    }  // end of getDistanceMatrixURL method

    // Function to get the location by coordinates using any objects
    internal fun getLocation(obj: Any?): List<Double> {
        // Return the specified list of coordinate if the object is not null
        return if (obj != null) {
            when (obj) {
                // Get the coordinates for Address
                is UserAddress? -> getCoordinates(obj.location)

                // Get the coordinates for Market
                is Market? -> getCoordinates(obj.location)

                else -> listOf(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)  // Default coordinates
            }
        } else {
            listOf(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)  // Default coordinates
        }
    }  // end of getLocation method

    // Function to get the exact coordinates of the specified location
    private fun getCoordinates(loc: LocationData?): List<Double> {
        // Return a list of coordinates if it is not null; otherwise, default
        return if (loc != null) listOf(loc.latitude, loc.longitude)
        else listOf(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }  // end of getCoordinates method

    // Function to get the current marker position in the map fragment
    internal fun getMarkerLocationData(coordinates: LatLng?): LocationData {
        return coordinates?.let {
            // The current available location
            LocationData(
                it.latitude, it.longitude,
                GeoFireUtils.getGeoHashForLocation(
                    GeoLocation(it.latitude, it.longitude)
                )
            )
        } ?: run {
            // Jose Rizal's house as the default coordinates
            LocationData(
                DEFAULT_LATITUDE, DEFAULT_LONGITUDE,
                GeoFireUtils.getGeoHashForLocation(
                    GeoLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
                )
            )
        }  // end of let & run
    }  // end of getGeoLocationData method

}  // end of Constants object
