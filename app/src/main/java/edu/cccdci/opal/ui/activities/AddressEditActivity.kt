package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityAddressEditBinding
import edu.cccdci.opal.dataclasses.UserAddress
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.FormValidation
import edu.cccdci.opal.utils.UtilityClass

class AddressEditActivity : UtilityClass(), View.OnClickListener, OnMapReadyCallback {

    private lateinit var binding: ActivityAddressEditBinding
    private lateinit var mAddressPlaces: Array<String>
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private val mProvinces: MutableList<HashMap<String, String>> = mutableListOf()
    private val mCities: MutableList<HashMap<String, String>> = mutableListOf()
    private var mSelectedProvinceID: String = ""
    private var mUserAddress: UserAddress? = null
    private var mCurrentMarkerPos: LatLng? = null
    private var mAddrHashMap: HashMap<String, Any?> = hashMapOf()
    private var mIsNewAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAddressEditBinding.inflate(layoutInflater)
        // Get the string array of address places (for alert dialogs)
        mAddressPlaces = resources.getStringArray(R.array.address_places)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.USER_ADDRESS)) {
            // Get data from the parcelable class
            mUserAddress = intent.getParcelableExtra(Constants.USER_ADDRESS)

            setSelectedAddressValues()  // Store the address data values
        }

        if (mUserAddress == null) {
            /* Set the Shared Preference of current marker position blank
             * whenever a user creates a new address.
             */
            mSPEditor.putString(Constants.CURRENT_MARKER_POS, "").apply()

            mIsNewAddress = true  // To indicate that this is for new addresses
        }

        // Prepare the SupportMapFragment
        mSupportMap = supportFragmentManager
            .findFragmentById(R.id.mpfr_address_map) as SupportMapFragment
        // Load the map fragment
        mSupportMap.getMapAsync(this@AddressEditActivity)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbAddressEditActivity, false)

            // Call the Firestore Function to retrieve province data
            FirestoreClass().getProvinces(this@AddressEditActivity)

            // Actions when one of the province items was selected
            actvAddrProvince.setOnItemClickListener { _, _, position, _ ->
                // Set the drop down data of city/municipality
                setCityValues(position)
            }

            // Actions when one of the city items was selected
            actvAddrCtm.setOnItemClickListener { _, _, position, _ ->
                // Set the drop down data of barangay
                setBarangayValues(position)
            }

            // Click event for Submit Address button
            btnSubmitAddress.setOnClickListener(this@AddressEditActivity)
            // Click event for Delete Address button
            btnDeleteAddress.setOnClickListener(this@AddressEditActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible again
    override fun onRestart() {
        super.onRestart()

        // Get current marker position from Shared Preferences
        val currentPosition = mSharedPrefs.getString(
            Constants.CURRENT_MARKER_POS, ""
        )!!

        /* If the current marker position is not empty, change the selected
         * address object and update the map fragment.
         */
        if (currentPosition.isNotEmpty()) {
            // Store the new current marker position
            mCurrentMarkerPos = Gson().fromJson(currentPosition, LatLng::class.java)

            // Update the map fragment
            mSupportMap.getMapAsync(this@AddressEditActivity)
        }
    }  // end of onRestart method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Saves the user's address
                R.id.btn_submit_address -> {
                    // Stores modified information (if any)
                    storeAddressChanges()

                    if (mIsNewAddress || mAddrHashMap.isNotEmpty()) {
                        // If there are any changes made, save address info
                        saveAddressChanges()
                    } else {
                        /* Exit the activity if there are no changes made.
                         * This is to prevent unnecessary reads and writes
                         * in Cloud Firestore.
                         */

                        // Displays the Toast message
                        toastMessage(
                            this@AddressEditActivity,
                            getString(R.string.msg_no_address_info_changed)
                        )

                        finish()  // Closes the current activity
                    }  // end of if-else
                }

                // Deletes the user's address
                R.id.btn_delete_address -> {
                    // Prevent deletion of default address
                    if (mUserAddress != null && mUserAddress!!.default) {
                        showSnackBar(
                            this@AddressEditActivity,
                            getString(R.string.err_delete_default_address),
                            true
                        )
                    }
                    // Display a dialog to confirm deletion of non-default address
                    else {
                        /* Display an alert dialog with two action buttons
                         * (Delete & Cancel)
                         */
                        DialogClass(this@AddressEditActivity).alertDialog(
                            getString(R.string.dialog_delete_address_title),
                            getString(R.string.dialog_delete_address_message),
                            getString(R.string.dialog_btn_delete),
                            getString(R.string.dialog_btn_cancel),
                            Constants.DELETE_ADDRESS_ACTION
                        )
                    }
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Override the back function
    override fun onBackPressed() {
        storeAddressChanges()  // Stores modified information (if any)

        // If there are any changes to the user profile information (New Address)
        if (mIsNewAddress && mAddrHashMap.isNotEmpty()) {
            // Display an alert dialog with two action buttons (Exit & Continue)
            DialogClass(this@AddressEditActivity).alertDialog(
                getString(R.string.dialog_new_address_title),
                getString(R.string.dialog_new_address_message),
                getString(R.string.dialog_btn_exit),
                getString(R.string.dialog_btn_continue),
                Constants.EXIT_ADDRESS_ACTION
            )
        }
        // If there are any changes to the user profile information (Edit Address)
        else if (mAddrHashMap.isNotEmpty()) {
            /* Display an alert dialog with three action buttons
             * (Save, Don't Save & Cancel)
             */
            DialogClass(this@AddressEditActivity).alertDialog(
                getString(R.string.dialog_edit_address_title),
                getString(R.string.dialog_edit_address_message),
                getString(R.string.dialog_btn_save),
                getString(R.string.dialog_btn_dont_save),
                getString(R.string.dialog_btn_cancel)
            )
        } else {
            super.onBackPressed()  // Default back operation
        }  // end of if-else if-else

    }  // end of onBackPressed method

    // Overriding function to set the Map UI of user's address location
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        mGoogleMap.apply {
            clear()  // Clear all the markers set in the map

            // Set the marker position to the current position if it is available
            val markerPosition = mCurrentMarkerPos ?: LatLng(
                // Jose Rizal's house as the default coordinates
                Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
            )

            // Make the marker visible to the Map UI
            addMarker(MarkerOptions().apply {
                // Set the position to the latitude and longitude of user's address
                position(markerPosition)
                // Customize the icon image
                icon(
                    BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_map_marker_primary)
                )
            })
            // Focus the Map UI to the position of marker
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    markerPosition, Constants.ZOOM_SMALL
                )
            )

            // Disable all touch interactions and toolbar of the Map UI
            uiSettings.setAllGesturesEnabled(false)
            uiSettings.isMapToolbarEnabled = false

            // Actions when the map is clicked
            setOnMapClickListener {
                // Create an Intent to launch MapActivity
                Intent(this@AddressEditActivity, MapActivity::class.java)
                    .run {
                        // Add the marker position to the intent
                        putExtra(Constants.CURRENT_MARKER_POS, markerPosition)

                        startActivity(this)  // Opens the Map Activity
                    }
            }
        }  // end of apply

    }  // end of onMapReady method

    // Function to store existing address data in the respective fields
    private fun setSelectedAddressValues() {
        with(binding) {
            mUserAddress?.let {
                // Fill up the available fields
                etAddrFullName.setText(it.fullName)
                etAddrPhone.setText(it.phoneNum)
                actvAddrProvince.setText(it.province)
                actvAddrCtm.setText(it.city)
                actvAddrBrgy.setText(it.barangay)
                etAddrPostal.setText(it.postal.toString())
                etAddrDetails.setText(it.detailAdd)
                smDefaultAddress.isChecked = it.default

                // Disable changing status of default address
                if (it.default) {
                    smDefaultAddress.setOnCheckedChangeListener { _, _ ->
                        // Forces the switch to remain checked
                        smDefaultAddress.isChecked = true

                        /* Shows a message that unselecting default address
                         * is not allowed
                         */
                        showSnackBar(
                            this@AddressEditActivity,
                            getString(R.string.err_unselect_default_address),
                            true
                        )
                    }
                }  // end of if

                // Prevents NullPointerException
                if (it.location != null) {
                    // Update the current marker position
                    mCurrentMarkerPos = LatLng(
                        it.location.latitude, it.location.longitude
                    )
                }

                // Set the current marker position to the existing user location
                mSPEditor.putString(
                    Constants.CURRENT_MARKER_POS,
                    /* Convert the LatLng object to JSON. If null,
                     * use default coordinates.
                     */
                    Gson().toJson(
                        mCurrentMarkerPos ?:
                        // Jose Rizal's house as the default coordinates
                        LatLng(
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
                        )
                    )
                ).apply()

                // Change the interface of Address Editor
                tvAddressEditTitle.setText(R.string.tlb_title_edit_address)
                btnDeleteAddress.visibility = View.VISIBLE
            }  // end of let
        }  // end of with(binding)

    }  // end of setSelectedAddressValues method

    // Function to supply the retrieved data for Province Spinner
    internal fun retrieveProvinces(prvList: List<HashMap<String, String>>) {
        // Clear the list of Province data if it has any
        if (mProvinces.isNotEmpty()) mProvinces.clear()

        // Variable to store the list of province names
        val provinces: MutableList<String> = mutableListOf()

        // Perform data storage if the retrieved list is not empty
        if (prvList.isNotEmpty()) {
            // Add the whole sorted list of Provinces
            mProvinces.addAll(prvList.sortedBy { it[Constants.PROVINCE_NAME] })
            // Also, store the province names in the another list
            mProvinces.forEach { provinces.add(it[Constants.PROVINCE_NAME]!!) }
        }

        with(binding) {
            // Prepare the drop down values for provinces
            actvAddrProvince.setAdapter(
                ArrayAdapter(
                    this@AddressEditActivity, R.layout.spinner_item, provinces
                )
            )

            // Get the selected province, if any
            val prov = actvAddrProvince.text.toString().trim { it <= ' ' }

            /* If the selected province exists in the list, proceed to set
             * drop down items for city/municipality
             */
            if (prov in provinces) {
                setCityValues(provinces.indexOf(prov))
            } else if (prov.isNotEmpty()) {
                // Clear the spinner text of province, city/municipality and barangay
                actvAddrProvince.text.clear()
                actvAddrCtm.text.clear()
                actvAddrBrgy.text.clear()

                // Display a dialog that the province data is non-existent
                DialogClass(this@AddressEditActivity).alertDialog(
                    getString(
                        R.string.dialog_no_place_found_title, mAddressPlaces[0]
                    ),
                    getString(
                        R.string.dialog_no_place_found_message, mAddressPlaces[0],
                        prov, mAddressPlaces[0]
                    ),
                    getString(R.string.dialog_btn_ok)
                )
            }
        }  // end of with(binding)

    }  // end of retrieveProvinces method

    // Function to supply the retrieved data for City/Municipality Spinner
    internal fun retrieveCities(ctList: List<HashMap<String, String>>) {
        // Clear the list of City/Municipality data if it has any
        if (mCities.isNotEmpty()) mCities.clear()

        // Variable to store the list of city/municipality names
        val cities: MutableList<String> = mutableListOf()

        // Perform data storage if the retrieved list is not empty
        if (ctList.isNotEmpty()) {
            // Add the whole sorted list of Cities
            mCities.addAll(ctList.sortedBy { it[Constants.CITY_NAME] })
            // Also, store the city names in the another list
            mCities.forEach { cities.add(it[Constants.CITY_NAME]!!) }
        }

        with(binding) {
            // Prepare the drop down values for cities
            actvAddrCtm.setAdapter(
                ArrayAdapter(
                    this@AddressEditActivity, R.layout.spinner_item, cities
                )
            )

            // Get the selected city, if any
            val ct = actvAddrCtm.text.toString().trim { it <= ' ' }

            /* If the selected city/municipality exists in the list, proceed to
             * set drop down items for barangay
             */
            if (ct in cities) {
                setBarangayValues(cities.indexOf(ct))
            } else if (ct.isNotEmpty()) {
                // Clear the spinner text of city/municipality and barangay
                actvAddrCtm.text.clear()
                actvAddrBrgy.text.clear()

                // Display a dialog that the city/municipality data is non-existent
                DialogClass(this@AddressEditActivity).alertDialog(
                    getString(
                        R.string.dialog_no_place_found_title, mAddressPlaces[1]
                    ),
                    getString(
                        R.string.dialog_no_place_found_message, mAddressPlaces[1],
                        ct, mAddressPlaces[1]
                    ),
                    getString(R.string.dialog_btn_ok)
                )
            }
        }  // end of with(binding)

    }  // end of retrieveCities method

    // Function to supply the retrieved data for Barangay Spinner
    internal fun retrieveBarangays(bgyList: List<String>) {
        with(binding) {
            // Prepare the drop down values for barangays
            actvAddrBrgy.setAdapter(
                ArrayAdapter(
                    this@AddressEditActivity, R.layout.spinner_item, bgyList
                )
            )

            // Get the selected barangay, if any
            val brgy = actvAddrBrgy.text.toString().trim { it <= ' ' }

            if (brgy.isNotEmpty() && brgy !in bgyList) {
                actvAddrBrgy.text.clear()  // Clear the spinner text of barangay

                // Display a dialog that the barangay data is non-existent
                DialogClass(this@AddressEditActivity).alertDialog(
                    getString(
                        R.string.dialog_no_place_found_title, mAddressPlaces[2]
                    ),
                    getString(
                        R.string.dialog_no_place_found_message, mAddressPlaces[2],
                        brgy, mAddressPlaces[2]
                    ),
                    getString(R.string.dialog_btn_ok)
                )
            }
        }  // end of with(binding)

    }  // end of retrieveBarangays method

    /* Function to set drop down data of city/municipality once
     * the user selects an item from province.
     */
    private fun setCityValues(position: Int) {
        with(binding) {
            // Store the selected province's ID
            mSelectedProvinceID = mProvinces[position][Constants.PROVINCE_ID]!!

            // Enable drop down functionality of city/municipality
            if (!actvAddrCtm.isEnabled) {
                tilAddrCtm.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrCtm.isEnabled = true
            }
            // Clear the drop down data of city/municipality for a new batch of data
            else if (actvAddrCtm.text.isNotEmpty()) {
                actvAddrCtm.text.clear()
            }

            // Disable drop down functionality of barangays
            if (actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_NONE
                actvAddrBrgy.isEnabled = false

                // Clear the drop down data of barangay if the field is not empty
                if (actvAddrBrgy.text.isNotEmpty()) actvAddrBrgy.text.clear()
            }

            // Call the Firestore function to retrieve city data
            FirestoreClass().getCities(
                this@AddressEditActivity, mSelectedProvinceID
            )
        }  // end of with(binding)

    }  // end of setCityValues method

    /* Function to set drop down data of barangay once the user
     * selects an item from city.
     */
    private fun setBarangayValues(position: Int) {
        with(binding) {
            // Enable the drop down functionality of barangays
            if (!actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrBrgy.isEnabled = true
            }
            // Clear the drop down data of barangay for a new batch of data
            else if (actvAddrBrgy.text.isNotEmpty()) {
                actvAddrBrgy.text.clear()
            }

            // Call the Firestore function to retrieve barangay data
            FirestoreClass().getBarangays(
                this@AddressEditActivity, mSelectedProvinceID,
                mCities[position][Constants.CITY_ID]!!
            )
        }  // end of with(binding)

    }  // end of setBarangayValues method

    // Function to validate user address
    private fun addressValidation(): Boolean {
        with(binding) {
            // Create a FormValidation object, and then execute the validations
            return FormValidation(this@AddressEditActivity).run {
                when {
                    // Full Name
                    !validateName(etAddrFullName) -> false
                    // Phone Number
                    !validatePhoneNumber(etAddrPhone) -> false
                    // Province
                    !checkSpinnerSelection(actvAddrProvince) -> false
                    // City/Municipality
                    !checkSpinnerSelection(actvAddrCtm) -> false
                    // Barangay
                    !checkSpinnerSelection(actvAddrBrgy) -> false
                    // Postal Code
                    !validatePostalCode(etAddrPostal) -> false
                    // Detailed Address
                    !validateLongTexts(etAddrDetails) -> false
                    // When all fields are valid
                    else -> true
                }  // end of when
            }  // end of run

        }  // end of with(binding)

    }  // end of addressValidation method

    // Function to save user's address
    internal fun saveAddressChanges() {
        // Validate first the address fields
        if (addressValidation()) {
            // Display the loading message
            showProgressDialog(
                this@AddressEditActivity, this@AddressEditActivity,
                if (mUserAddress != null) getString(R.string.msg_saving_changes)
                else getString(R.string.msg_please_wait)
            )

            // If default address status was changed
            if (mAddrHashMap[Constants.DEFAULT_ADDR] != null)
                FirestoreClass().findDefaultAddress(this@AddressEditActivity)
            else
                addOrUpdateAddress()  // Adding or updating address information
        }  // end of if

    }  // end of saveAddressChanges method

    // Function to add (null mAddress) or update user address to Firestore
    internal fun addOrUpdateAddress() {
        // If mAddress object is null, add a new address
        if (mUserAddress == null) {
            // Get the document reference for the new address
            val addressRef = FirestoreClass().getUserAddressReference()

            with(binding) {
                // Object to store user address data
                mUserAddress = UserAddress(
                    Constants.ADDRESS_ID_TEMP + addressRef.id,
                    etAddrFullName.text.toString().trim { it <= ' ' },
                    etAddrPhone.text.toString().trim { it <= ' ' },
                    actvAddrProvince.text.toString().trim { it <= ' ' },
                    actvAddrCtm.text.toString().trim { it <= ' ' },
                    actvAddrBrgy.text.toString().trim { it <= ' ' },
                    etAddrPostal.text.toString().trim { it <= ' ' }.toInt(),
                    etAddrDetails.text.toString().trim { it <= ' ' },
                    smDefaultAddress.isChecked,
                    Constants.getMarkerLocationData(mCurrentMarkerPos)
                )
            }  // end of with(binding)

            // Adds the user address data in the Firestore Database
            FirestoreClass().addUserAddress(
                this@AddressEditActivity, mUserAddress!!
            )
        }
        // If mAddress has a value, update the current address
        else {
            FirestoreClass().updateAddress(
                this@AddressEditActivity, mUserAddress!!.addressID, mAddrHashMap
            )
        }  // end of if-else

    }  // end of addOrUpdateAddress method

    // Function to store modified user address information
    private fun storeAddressChanges() {
        // Clear the HashMap first for a new batch of modified information
        mAddrHashMap.clear()

        /* Store temporary object for comparison purposes if the page is for
         * creating new address
         */
        if (mIsNewAddress) mUserAddress = UserAddress()

        with(binding) {
            mUserAddress?.let { ad ->
                val fullName = etAddrFullName.text.toString().trim { it <= ' ' }
                // Save the new full name if it is different from previous full name
                if (fullName != ad.fullName)
                    mAddrHashMap[Constants.FULL_NAME] = fullName

                val phoneNumber = etAddrPhone.text.toString().trim { it <= ' ' }
                // Save the new phone number if it is different from previous phone number
                if (phoneNumber != ad.phoneNum)
                    mAddrHashMap[Constants.PHONE_NUM] = phoneNumber

                val province = actvAddrProvince.text.toString().trim { it <= ' ' }
                // Save the new province if it is different from previous province
                if (province != ad.province)
                    mAddrHashMap[Constants.PROVINCE] = province

                val city = actvAddrCtm.text.toString().trim { it <= ' ' }
                /* Save the new city/municipality if it is different
                 * from previous city/municipality
                 */
                if (city != ad.city)
                    mAddrHashMap[Constants.CITY] = city

                val barangay = actvAddrBrgy.text.toString().trim { it <= ' ' }
                // Save the new barangay if it is different from previous barangay
                if (barangay != ad.barangay)
                    mAddrHashMap[Constants.BARANGAY] = barangay

                val postalCode = if (etAddrPostal.text!!.isNotEmpty())
                    etAddrPostal.text.toString().trim { it <= ' ' }.toInt()
                else
                    0  // Default value to prevent NumberFormatException
                // Save the new postal code if it is different from previous postal code
                if (postalCode != ad.postal)
                    mAddrHashMap[Constants.POSTAL] = postalCode

                val detailAdd = etAddrDetails.text.toString().trim { it <= ' ' }
                /* Save the new detailed address if it is different
                 * from previous detailed address
                 */
                if (detailAdd != ad.detailAdd)
                    mAddrHashMap[Constants.DETAIL_ADDR] = detailAdd

                val defaultAdd = smDefaultAddress.isChecked
                /* Save the new default address toggle if it is
                 * the opposite of the previous toggle
                 */
                if (defaultAdd != ad.default)
                    mAddrHashMap[Constants.DEFAULT_ADDR] = defaultAdd

                val addrLocation = if (mCurrentMarkerPos != null)
                    Constants.getMarkerLocationData(mCurrentMarkerPos)
                else
                    null  // Default value
                /* Save the new address location if it is different from
                 * the previous address location
                 */
                if (addrLocation != ad.location)
                    mAddrHashMap[Constants.LOCATION] = addrLocation
            }  // end of let
        }  // end of with(binding)

        // Revert back to null if the page is for creating new address
        if (mIsNewAddress) mUserAddress = null
    }  // end of storeAddressChanges method

    // Function to delete user's address
    internal fun deleteUserAddress() {
        // Check if the mSelectedAddress object is not null
        if (mUserAddress != null) {
            // Display the loading message
            showProgressDialog(
                this@AddressEditActivity, this@AddressEditActivity,
                getString(R.string.msg_please_wait)
            )

            // Deletes the user address data in the Firestore Database
            FirestoreClass().deleteAddress(
                this@AddressEditActivity, mUserAddress!!.addressID
            )
        }  // end of if
    }  // end of deleteUserAddress method

    // Function to prompt user that the address is saved or deleted
    internal fun addressSavedOrDeleted(isModified: Boolean) {
        hideProgressDialog()  // Hide the loading message

        // Display a Toast message
        toastMessage(
            this@AddressEditActivity,
            if (isModified)
                getString(R.string.msg_address_saved)
            else
                getString(R.string.msg_address_deleted)
        )

        finish()  // Closes the activity
    }  // end of addressSavedOrDeleted method

}  // end of AddressEditActivity class
