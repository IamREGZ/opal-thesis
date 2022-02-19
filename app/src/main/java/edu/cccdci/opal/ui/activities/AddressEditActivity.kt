package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
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
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.dataclasses.Location
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class AddressEditActivity : UtilityClass(), View.OnClickListener, OnMapReadyCallback {

    private lateinit var binding: ActivityAddressEditBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private val mProvinces: MutableList<HashMap<String, String>> = mutableListOf()
    private val mProvNames: MutableList<String> = mutableListOf()
    private val mCities: MutableList<HashMap<String, String>> = mutableListOf()
    private val mCTNames: MutableList<String> = mutableListOf()
    private var mSelectedProvince: String = ""
    private var mAddress: Address? = null
    private var mCurrentMarkerPos: LatLng? = null
    private var mAddrHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = ActivityAddressEditBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.USER_ADDRESS)) {
            // Get data from the parcelable class
            mAddress = intent.getParcelableExtra(Constants.USER_ADDRESS)
        }

        // Store the address data values if it is not null (prevents NPE)
        if (mAddress != null) {
            setSelectedAddressValues()
        } else {
            /* Set the Shared Preference of current marker position blank
             * whenever a user creates a new address.
             */
            mSPEditor.putString(Constants.CURRENT_MARKER_POS, "").apply()
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

            // Prepare the drop down values for provinces
            val provinceAdapter = ArrayAdapter(
                this@AddressEditActivity, R.layout.spinner_item, mProvNames
            )
            actvAddrProvince.setAdapter(provinceAdapter)

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
                R.id.btn_submit_address -> saveUserAddress()
                // Deletes the user's address
                R.id.btn_delete_address -> deleteUserAddress()
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Overriding function to set the Map UI of user's address location
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        mGoogleMap.apply {
            clear()  // Clear all the markers set in the map

            val markerPosition = if (mCurrentMarkerPos != null) {
                // Set the marker position to the current position if it is available
                mCurrentMarkerPos!!
            }
            else {
                // Jose Rizal's house as the default coordinates
                LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE)
            }

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
            moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 18f))

            // Disable all touch interactions and toolbar of the Map UI
            uiSettings.setAllGesturesEnabled(false)
            uiSettings.isMapToolbarEnabled = false

            // Actions when the map is clicked
            setOnMapClickListener {
                // Create an Intent to launch MapActivity
                val intent = Intent(
                    this@AddressEditActivity, MapActivity::class.java
                )

                // Add the marker position to the intent
                intent.putExtra(
                    Constants.CURRENT_MARKER_POS, markerPosition
                )

                startActivity(intent)  // Opens the Map Activity
            }
        }  // end of apply

    }  // end of onMapReady method

    // Function to store existing address data in the respective fields
    private fun setSelectedAddressValues() {
        with(binding) {
            // Fill up the available fields
            etAddrFullName.setText(mAddress!!.fullName)
            etAddrPhone.setText(mAddress!!.phoneNum)
            actvAddrProvince.setText(mAddress!!.province)
            actvAddrCtm.setText(mAddress!!.city)
            actvAddrBrgy.setText(mAddress!!.barangay)
            etAddrPostal.setText(mAddress!!.postal.toString())
            etAddrDetails.setText(mAddress!!.detailAdd)
            smDefaultAddress.isChecked = mAddress!!.default

            // Set the current marker position to the existing user location
            mSPEditor.putString(
                Constants.CURRENT_MARKER_POS,
                // Prevents NullPointerException
                if (mAddress!!.location != null) {
                    // Update the current marker position
                    mCurrentMarkerPos = LatLng(
                        mAddress!!.location!!.latitude,
                        mAddress!!.location!!.longitude
                    )

                    // Convert the LatLng object to JSON. If null, use default coordinates
                    Gson().toJson(
                        mCurrentMarkerPos ?:
                        // Jose Rizal's house as the default coordinates
                        LatLng(
                            Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
                        )
                    )
                } else {
                    // Use blank string to indicate there's no existing current location
                    ""
                }
            ).apply()

            // Change the interface of Address Info
            tvAddressEditTitle.setText(R.string.tlb_title_edit_address)
            btnDeleteAddress.visibility = View.VISIBLE
        }  // end of with(binding)

    }  // end of setSelectedAddressValues method

    // Function to supply the retrieved data for Province Spinner
    fun retrieveProvinces(prvList: List<HashMap<String, String>>) {
        // Clear the list of Province Data if it has any
        if (mProvinces.isNotEmpty())
            mProvinces.clear()
        // Clear the list of Province Names if it has any
        if (mProvNames.isNotEmpty())
            mProvNames.clear()

        // Perform data storage if the retrieved list is not empty
        if (prvList.isNotEmpty()) {
            // Add the whole sorted list of Provinces
            mProvinces.addAll(prvList.sortedBy { it[Constants.PROVINCE_NAME] })
            // Also, store the province names in the another list
            mProvinces.forEach { mProvNames.add(it[Constants.PROVINCE_NAME]!!) }
        }
    }  // end of retrieveProvinces method

    // Function to supply the retrieved data for City/Municipality Spinner
    fun retrieveCities(ctList: List<HashMap<String, String>>) {
        // Clear the list of City Data if it has any
        if (mCities.isNotEmpty())
            mCities.clear()
        // Clear the list of City Names if it has any
        if (mCTNames.isNotEmpty())
            mCTNames.clear()

        // Perform data storage if the retrieved list is not empty
        if (ctList.isNotEmpty()) {
            // Add the whole sorted list of Cities
            mCities.addAll(ctList.sortedBy { it[Constants.CITY_NAME] })
            // Also, store the city names in the another list
            mCities.forEach { mCTNames.add(it[Constants.CITY_NAME]!!) }
        }
    }  // end of retrieveCities method

    /* Function to set drop down data of city/municipality once
     * the user selects an item from province.
     */
    private fun setCityValues(position: Int) {
        with(binding) {
            // Store the selected province's ID
            mSelectedProvince = mProvinces[position][Constants.PROVINCE_ID]!!

            // Clear the drop down data of city/municipality for a new batch of data
            actvAddrCtm.text.clear()

            // Call the Firestore function to retrieve city data
            FirestoreClass().getCities(
                this@AddressEditActivity, mSelectedProvince
            )

            // Prepare the drop down values for cities
            val cityAdapter = ArrayAdapter(
                this@AddressEditActivity, R.layout.spinner_item, mCTNames
            )
            actvAddrCtm.setAdapter(cityAdapter)

            // Enable drop down functionality of city/municipality
            if (!actvAddrCtm.isEnabled) {
                tilAddrCtm.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrCtm.isEnabled = true
            }

            // Disable drop down functionality of barangays
            if (actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_NONE
                actvAddrBrgy.isEnabled = false
            }

            // Clear the drop down data of barangay for a new batch of data later
            actvAddrBrgy.text.clear()
        }  // end of with(binding)

    }  // end of setCityValues method

    /* Function to set drop down data of barangay once the user
     * selects an item from city.
     */
    private fun setBarangayValues(position: Int) {
        with(binding) {
            /* Clear the drop down data of barangay if it the spinner is
             * enabled. This is to prevent deleting an empty data set
             * whenever the province has changed selection.
             */
            if (actvAddrBrgy.isEnabled) actvAddrBrgy.text.clear()

            // Call the Firestore function to retrieve barangay data
            val brgyResult = FirestoreClass().getBarangays(
                this@AddressEditActivity, mSelectedProvince,
                mCities[position][Constants.CITY_ID]!!
            )

            // Prepare the drop down values for barangays
            val brgyAdapter = ArrayAdapter(
                this@AddressEditActivity, R.layout.spinner_item, brgyResult
            )
            actvAddrBrgy.setAdapter(brgyAdapter)

            // Enable the drop down functionality of barangays
            if (!actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrBrgy.isEnabled = true
            }
        }  // end of with(binding)

    }  // end of setBarangayValues method

    // Function to validate user address
    private fun addressValidation(): Boolean {
        with(binding) {
            return when {
                // If the Full Name field is empty
                TextUtils.isEmpty(etAddrFullName.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_fullname),
                        true
                    )
                    false  // return false
                }

                // If the Phone Number field is empty
                TextUtils.isEmpty(etAddrPhone.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_phone),
                        true
                    )
                    false  // return false
                }

                // If no province is selected
                TextUtils.isEmpty(actvAddrProvince.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_province),
                        true
                    )
                    false  // return false
                }

                // If no city/municipality is selected
                TextUtils.isEmpty(actvAddrCtm.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_city),
                        true
                    )
                    false  // return false
                }

                // If no barangay is selected
                TextUtils.isEmpty(actvAddrBrgy.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_brgy),
                        true
                    )
                    false  // return false
                }

                // If the Postal Code field is empty
                TextUtils.isEmpty(etAddrPostal.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_postal),
                        true
                    )
                    false  // return false
                }

                // If the Detailed Address field is empty
                TextUtils.isEmpty(etAddrDetails.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@AddressEditActivity,
                        resources.getString(R.string.err_blank_detailed),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all inputs are valid
            }  // end of when

        }  // end of with(binding)

    }  // end of addressValidation method

    // Function to save user's address
    private fun saveUserAddress() {
        // Validate first the address fields
        if (addressValidation()) {
            // Display the loading message
            showProgressDialog(
                this@AddressEditActivity, this@AddressEditActivity,
                resources.getString(R.string.msg_please_wait)
            )

            if (mAddress == null) {
                // If mAddress object is null, add a new address
                addNewUserAddress()
            } else {
                // If mAddress has a value, update the current address
                updateUserAddress()
            }  // end of if-else
        }  // end of if
    }  // end of saveUserAddress method

    // Function to add user address to Firestore (if mAddress is null)
    private fun addNewUserAddress() {
        // Get the document reference for the new address
        val addressRef = FirestoreClass().getUserAddressReference()

        with(binding) {
            // Object to store user address data
            mAddress = Address(
                Constants.ADDRESS_ID_TEMP + addressRef.id,
                etAddrFullName.text.toString().trim { it <= ' ' },
                etAddrPhone.text.toString().trim { it <= ' ' },
                actvAddrProvince.text.toString().trim { it <= ' ' },
                actvAddrCtm.text.toString().trim { it <= ' ' },
                actvAddrBrgy.text.toString().trim { it <= ' ' },
                etAddrPostal.text.toString().trim { it <= ' ' }.toInt(),
                etAddrDetails.text.toString().trim { it <= ' ' },
                smDefaultAddress.isChecked,
                Location(
                    mCurrentMarkerPos!!.latitude,
                    mCurrentMarkerPos!!.longitude,
                    GeoFireUtils.getGeoHashForLocation(GeoLocation(
                        mCurrentMarkerPos!!.latitude,
                        mCurrentMarkerPos!!.longitude,
                    ))
                )
            )
        }

        // Adds the user address data in the Firestore Database
        FirestoreClass().addUserAddress(
            this@AddressEditActivity, mAddress!!
        )
    }  // end of addNewUserAddress method

    // Function to update existing user address (if mAddress is not null)
    private fun updateUserAddress() {
        storeUserAddressChanges()  // Stores modified information, if any

        // Proceed to update fields in the Cloud Firestore
        FirestoreClass().updateAddress(
            this@AddressEditActivity, mAddress!!.addressID,
            mAddrHashMap
        )
    }  // end of updateUserAddress method

    // Function to store modified user address information
    private fun storeUserAddressChanges() {
        // Clear the HashMap first for a new batch of modified information
        mAddrHashMap.clear()

        with(binding) {
            val fullName = etAddrFullName.text.toString().trim { it <= ' ' }
            // Save the new full name if it is different from previous full name
            if (fullName != mAddress!!.fullName)
                mAddrHashMap[Constants.FULL_NAME] = fullName

            val phoneNumber = etAddrPhone.text.toString().trim { it <= ' ' }
            // Save the new phone number if it is different from previous phone number
            if (phoneNumber != mAddress!!.phoneNum)
                mAddrHashMap[Constants.PHONENUM] = phoneNumber

            val province = actvAddrProvince.text.toString().trim { it <= ' ' }
            // Save the new province if it is different from previous province
            if (province != mAddress!!.province)
                mAddrHashMap[Constants.PROVINCE] = province

            val city = actvAddrCtm.text.toString().trim { it <= ' ' }
            /* Save the new city/municipality if it is different
             * from previous city/municipality
             */
            if (city != mAddress!!.city)
                mAddrHashMap[Constants.CITY] = city

            val barangay = actvAddrBrgy.text.toString().trim { it <= ' ' }
            // Save the new barangay if it is different from previous barangay
            if (barangay != mAddress!!.barangay)
                mAddrHashMap[Constants.BARANGAY] = barangay

            val postalCode = etAddrPostal.text.toString().trim { it <= ' ' }.toInt()
            // Save the new postal code if it is different from previous postal code
            if (postalCode != mAddress!!.postal)
                mAddrHashMap[Constants.POSTAL] = postalCode

            val detailAdd = etAddrDetails.text.toString().trim { it <= ' ' }
            /* Save the new detailed address if it is different
             * from previous detailed address
             */
            if (detailAdd != mAddress!!.detailAdd)
                mAddrHashMap[Constants.DETAIL_ADDR] = detailAdd

            val defaultAdd = smDefaultAddress.isChecked
            /* Save the new default address toggle if it is
             * the opposite of the previous toggle
             */
            if (defaultAdd != mAddress!!.default)
                mAddrHashMap[Constants.DEFAULT_ADDR] = defaultAdd

            val addrLocation = Location(
                mCurrentMarkerPos!!.latitude,
                mCurrentMarkerPos!!.longitude,
                GeoFireUtils.getGeoHashForLocation(GeoLocation(
                    mCurrentMarkerPos!!.latitude,
                    mCurrentMarkerPos!!.longitude,
                ))
            )
            /* Save the new address location if it is different from
             * the previous address location
             */
            if (addrLocation != mAddress!!.location!!)
                mAddrHashMap[Constants.LOCATION] = addrLocation
        }  // end of with(binding)

    }  // end of storeUserAddressChanges method

    // Function to prompt user that the address is saved
    fun addressSavedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Display a Toast message
        toastMessage(
            this@AddressEditActivity,
            resources.getString(R.string.msg_address_saved)
        )

        finish()  // Closes the activity

    }  // end of addressSavedPrompt method

    // Function to delete user's address
    private fun deleteUserAddress() {
        // Check if the mSelectedAddress object is not null
        if (mAddress != null) {
            // Display the loading message
            showProgressDialog(
                this@AddressEditActivity, this@AddressEditActivity,
                resources.getString(R.string.msg_please_wait)
            )

            // Deletes the user address data in the Firestore Database
            FirestoreClass().deleteAddress(
                this@AddressEditActivity, mAddress!!.addressID
            )
        }  // end of if
    }  // end of deleteUserAddress method

    // Function to prompt user that the address was deleted
    fun addressDeletedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Display a Toast message
        toastMessage(
            this@AddressEditActivity, resources.getString(R.string.msg_address_deleted)
        )

        finish()  // Closes the activity
    }  // end of addressDeletedPrompt method

}  // end of AddressEditActivity class