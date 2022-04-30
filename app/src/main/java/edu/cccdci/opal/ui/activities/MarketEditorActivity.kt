package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMarketEditorBinding
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.*
import java.io.IOException

class MarketEditorActivity : UtilityClass(), View.OnClickListener,
    View.OnLongClickListener, OnMapReadyCallback {

    private lateinit var binding: ActivityMarketEditorBinding
    private lateinit var mCategoryList: Array<String>
    private lateinit var mAddressPlaces: Array<String>
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private val mProvinces: MutableList<HashMap<String, String>> = mutableListOf()
    private val mCities: MutableList<HashMap<String, String>> = mutableListOf()
    private var mSelectedProvinceID: String = ""
    private var mMarketInfo: Market? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mTempMarketImageURL: String = ""
    private var mCategoryPos: Int = 0
    private var mCurrentMarkerPos: LatLng? = null
    private var mMarketHashMap: HashMap<String, Any?> = hashMapOf()
    private var mIsNewMarket: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMarketEditorBinding.inflate(layoutInflater)

        // Get the string array of market categories
        mCategoryList = resources.getStringArray(R.array.market_categories_list)
        // Get the string array of address places (for alert dialogs)
        mAddressPlaces = resources.getStringArray(R.array.address_places)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.MARKET_INFO)) {
            // Get the parcelable data from intent
            mMarketInfo = intent.getParcelableExtra(Constants.MARKET_INFO)

            setUserMarketValues()  // Store the market data values
        }

        if (mMarketInfo == null) {
            /* Set the Shared Preference of current marker position blank
             * whenever a user registers for wet market.
             */
            mSPEditor.putString(Constants.CURRENT_MARKER_POS, "").apply()

            mIsNewMarket = true  // To indicate that this is for vendor registration
        }

        // Prepare the SupportMapFragment
        mSupportMap = supportFragmentManager
            .findFragmentById(R.id.mpfr_market_address_map) as SupportMapFragment
        // Load the map fragment
        mSupportMap.getMapAsync(this@MarketEditorActivity)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMarketEditActivity, false)

            // Prepare the drop down values for market categories
            actvMktEditCategory.setAdapter(
                ArrayAdapter(
                    this@MarketEditorActivity, R.layout.spinner_item,
                    resources.getStringArray(R.array.market_categories_list)
                )
            )

            // Call the Firestore Function to retrieve province data
            FirestoreClass().getProvinces(this@MarketEditorActivity)

            // Actions when one of the province items was selected
            actvMktEditProvince.setOnItemClickListener { _, _, position, _ ->
                // Set the drop down data of city/municipality
                setCityValues(position)
            }

            // Actions when one of the city items was selected
            actvMktEditCtm.setOnItemClickListener { _, _, position, _ ->
                // Set the drop down data of barangay
                setBarangayValues(position)
            }

            // Actions when one of the market category items is selected
            actvMktEditCategory.setOnItemClickListener { _, _, position, _ ->
                /* If the Others is selected, display the Specify Category
                 * text field to specify the category
                 */
                if (mCategoryList[position] == Constants.ITEM_OTHERS) {
                    tilMktEditOtherSpec.visibility = View.VISIBLE
                }
                // Hide the text field and clear the text
                else {
                    etMktEditOtherSpec.text!!.clear()
                    tilMktEditOtherSpec.visibility = View.GONE
                }

                // Set the current category index to the selected item index
                mCategoryPos = position
            }

            // Click event for Market Image Selector Layout
            llMarketImageSelector.setOnClickListener(this@MarketEditorActivity)
            // Long click event for Market Image Selector Layout
            llMarketImageSelector.setOnLongClickListener(this@MarketEditorActivity)
            // Click event for Submit Market Button
            btnSubmitMarket.setOnClickListener(this@MarketEditorActivity)
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
            mSupportMap.getMapAsync(this@MarketEditorActivity)
        }
    }  // end of onRestart method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Change the market image
                R.id.ll_market_image_selector -> {
                    // If storage permission access is already granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Launch the Image Selection
                        Constants.showImageSelection(this@MarketEditorActivity)
                    } else {
                        // Request a storage access permission to the device
                        ActivityCompat.requestPermissions(
                            this@MarketEditorActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }  // end of if-else
                }

                // Saves market information
                R.id.btn_submit_market -> {
                    // Stores modified information, if any
                    storeMarketInfoChanges()

                    if (mIsNewMarket || mMarketHashMap.isNotEmpty()) {
                        // If there are any changes made, save market info
                        saveMarketChanges()
                    } else {
                        /* Exit the activity if there are no changes made.
                         * This is to prevent unnecessary reads and writes
                         * in Cloud Firestore.
                         */

                        // Displays the Toast message
                        toastMessage(
                            this@MarketEditorActivity,
                            getString(R.string.msg_no_market_info_changed)
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
                // Clear the market's profile photo
                R.id.ll_market_image_selector -> {
                    if (mTempMarketImageURL.isNotEmpty()) {
                        /* Display an alert dialog with two action buttons
                         * (Remove & Cancel)
                         */
                        DialogClass(this@MarketEditorActivity).alertDialog(
                            getString(R.string.dialog_market_pic_remove_title),
                            getString(R.string.dialog_market_pic_remove_message),
                            getString(R.string.dialog_btn_remove),
                            getString(R.string.dialog_btn_cancel),
                            Constants.DELETE_MARKET_IMAGE_ACTION
                        )
                    } else {
                        // Display an error message
                        showSnackBar(
                            this@MarketEditorActivity,
                            getString(R.string.err_no_market_pic_to_remove),
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
        storeMarketInfoChanges()  // Stores modified information, if any

        // If there are any changes to the market information (Vendor Registration)
        if (mIsNewMarket && mMarketHashMap.isNotEmpty()) {
            // Display an alert dialog with two action buttons (Exit & Continue)
            DialogClass(this@MarketEditorActivity).alertDialog(
                getString(R.string.dialog_vendor_reg_title),
                getString(R.string.dialog_vendor_reg_message),
                getString(R.string.dialog_btn_exit),
                getString(R.string.dialog_btn_continue),
                Constants.EXIT_VENDOR_REG_ACTION
            )
        }
        // If there are any changes to the market information (Edit Market)
        else if (mMarketHashMap.isNotEmpty()) {
            /* Display an alert dialog with three action buttons
             * (Save, Don't Save & Cancel)
             */
            DialogClass(this@MarketEditorActivity).alertDialog(
                getString(R.string.dialog_edit_market_title),
                getString(R.string.dialog_edit_market_message),
                getString(R.string.dialog_btn_save),
                getString(R.string.dialog_btn_dont_save),
                getString(R.string.dialog_btn_cancel)
            )
        } else {
            super.onBackPressed()
        }  // end of if-else if-else

    }  // end of onBackPressed method

    // Function to check if storage permission is granted or denied
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If the user grants storage permission access
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Launch the Image Selection
                Constants.showImageSelection(this@MarketEditorActivity)
            } else {
                // If the user denies the permission access
                showSnackBar(
                    this@MarketEditorActivity,
                    getString(R.string.err_storage_permission_denied),
                    true
                )
            }  // end of if-else

        }  // end of if
    }  // end of onRequestPermissionsResult method

    // Function to change image in the product ImageView
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
                GlideLoader(this@MarketEditorActivity)
                    .loadImage(
                        mSelectedImageFileURI!!, binding.ivVenMarketImage
                    )

                // Set the temporary image URL to the URI of selected image
                mTempMarketImageURL = mSelectedImageFileURI.toString()

                // Make the "Long Press to Remove" instruction visible
                binding.tvDeleteMarketPicLabel.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@MarketEditorActivity,
                    getString(R.string.err_image_selection_failed)
                )
            }  // end of try-catch

        }  // end of if

    }  // end of onActivityResult method

    // Overriding function to set the Map UI of market's location
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        mGoogleMap.apply {
            clear()  // Clear all the markers set in the map

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
                Intent(this@MarketEditorActivity, MapActivity::class.java)
                    .run {
                        // Add the marker position to the intent
                        putExtra(Constants.CURRENT_MARKER_POS, markerPosition)

                        startActivity(this)  // Opens the Map Activity
                    }
            }
        }  // end of apply

    }  // end of onMapReady method

    // Function to store existing user's market data in the respective fields
    private fun setUserMarketValues() {
        with(binding) {
            mMarketInfo?.let {
                // Market Name and Parent Wet Market
                etMktEditName.setText(it.name)
                etMktEditWetMkt.setText(it.wetMarket)

                /* Set the current category index to the retrieved market's
                 * category index
                 */
                mCategoryPos = it.category
                // Get the text from category list array and set its value
                actvMktEditCategory.setText(mCategoryList[mCategoryPos])

                /* If the category is "Others," set the Specify Others field
                 * with the specified other category.
                 */
                if (mCategoryList[mCategoryPos] == Constants.ITEM_OTHERS) {
                    tilMktEditOtherSpec.visibility = View.VISIBLE
                    etMktEditOtherSpec.setText(it.otherCat)
                }

                // Market Address
                actvMktEditProvince.setText(it.province)
                actvMktEditCtm.setText(it.city)
                actvMktEditBrgy.setText(it.barangay)
                etMktEditPostal.setText(it.postal.toString())
                etMktEditDetails.setText(it.detailAdd)

                // Load the market image
                GlideLoader(this@MarketEditorActivity).loadImage(
                    it.image, ivVenMarketImage
                )

                // Store the temporary URL of market's profile picture
                mTempMarketImageURL = it.image

                /* Make the "Long Press to Remove" instruction visible
                 * if there's any image uploaded
                 */
                if (mTempMarketImageURL.isNotEmpty())
                    tvDeleteMarketPicLabel.visibility = View.VISIBLE

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

                // Change the interface of Market Editor
                tvMarketEditTitle.text = getString(R.string.tlb_title_edit_market)
                llVendorRegPanel.visibility = View.GONE

                /* To ensure that there are no validation anomalies while editing
                 * market info
                 */
                fblVendorTAndC.visibility = View.GONE
                cbVendorTAndC.isChecked = true
                cbVendorTAndC.isEnabled = false
            }  // end of let
        }  // end of with(binding)

    }  // end of setUserMarketValues method

    // Function to remove market's profile picture
    internal fun removeMarketImage() {
        // Set the market profile photo to default placeholder
        GlideLoader(this@MarketEditorActivity)
            .loadImage("", binding.ivVenMarketImage)

        // Clear all image selection values
        mTempMarketImageURL = ""
        mSelectedImageFileURI = null

        // Make the "Long Press to Remove" instruction not visible
        binding.tvDeleteMarketPicLabel.visibility = View.GONE

        // Display a success message
        showSnackBar(
            this@MarketEditorActivity,
            getString(R.string.msg_market_pic_removed),
            false
        )
    }  // end of removeMarketImage method

    // Function to supply the retrieved data for Province Spinner
    internal fun retrieveProvinces(prvList: List<HashMap<String, String>>) {
        // Clear the list of Province Data if it has any
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
            actvMktEditProvince.setAdapter(
                ArrayAdapter(
                    this@MarketEditorActivity, R.layout.spinner_item, provinces
                )
            )

            // Get the selected province, if any
            val prov = actvMktEditProvince.text.toString().trim { it <= ' ' }

            /* If the selected province exists in the list, proceed to set
             * drop down items for city/municipality
             */
            if (prov in provinces) {
                setCityValues(provinces.indexOf(prov))
            } else if (prov.isNotEmpty()) {
                // Clear the spinner text of province, city/municipality and barangay
                actvMktEditProvince.text.clear()
                actvMktEditCtm.text.clear()
                actvMktEditBrgy.text.clear()

                // Display a dialog that the province data is non-existent
                DialogClass(this@MarketEditorActivity).alertDialog(
                    getString(
                        R.string.dialog_no_place_found_title,
                        mAddressPlaces[0]
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
    fun retrieveCities(ctList: List<HashMap<String, String>>) {
        // Clear the list of City Data if it has any
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
            actvMktEditCtm.setAdapter(
                ArrayAdapter(
                    this@MarketEditorActivity, R.layout.spinner_item, cities
                )
            )

            // Get the selected city, if any
            val ct = actvMktEditCtm.text.toString().trim { it <= ' ' }

            /* If the selected city/municipality exists in the list, proceed to
             * set drop down items for barangay
             */
            if (ct in cities) {
                setBarangayValues(cities.indexOf(ct))
            } else if (ct.isNotEmpty()) {
                // Clear the spinner text of city/municipality and barangay
                actvMktEditCtm.text.clear()
                actvMktEditBrgy.text.clear()

                // Display a dialog that the city/municipality data is non-existent
                DialogClass(this@MarketEditorActivity).alertDialog(
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
            actvMktEditBrgy.setAdapter(
                ArrayAdapter(
                    this@MarketEditorActivity, R.layout.spinner_item, bgyList
                )
            )

            // Get the selected barangay, if any
            val brgy = actvMktEditBrgy.text.toString().trim { it <= ' ' }

            if (brgy.isNotEmpty() && brgy !in bgyList) {
                actvMktEditBrgy.text.clear()  // Clear the spinner text of barangay

                // Display a dialog that the barangay data is non-existent
                DialogClass(this@MarketEditorActivity).alertDialog(
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
            if (!actvMktEditCtm.isEnabled) {
                tilMktEditCtm.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvMktEditCtm.isEnabled = true
            }
            // Clear the drop down data of city/municipality for a new batch of data
            else if (actvMktEditCtm.text.isNotEmpty()) {
                actvMktEditCtm.text.clear()
            }

            // Disable drop down functionality of barangays
            if (actvMktEditBrgy.isEnabled) {
                tilMktEditBrgy.endIconMode = TextInputLayout.END_ICON_NONE
                actvMktEditBrgy.isEnabled = false

                // Clear the drop down data of barangay if the field is not empty
                if (actvMktEditBrgy.text.isNotEmpty()) actvMktEditBrgy.text.clear()
            }

            // Call the Firestore function to retrieve city data
            FirestoreClass().getCities(
                this@MarketEditorActivity, mSelectedProvinceID
            )
        }  // end of with(binding)

    }  // end of setCityValues method

    /* Function to set drop down data of barangay once the user
     * selects an item from city.
     */
    private fun setBarangayValues(position: Int) {
        with(binding) {
            // Enable the drop down functionality of barangays
            if (!actvMktEditBrgy.isEnabled) {
                tilMktEditBrgy.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvMktEditBrgy.isEnabled = true
            }
            // Clear the drop down data of barangay for a new batch of data
            else if (actvMktEditBrgy.text.isNotEmpty()) {
                actvMktEditBrgy.text.clear()
            }

            // Call the Firestore function to retrieve barangay data
            FirestoreClass().getBarangays(
                this@MarketEditorActivity, mSelectedProvinceID,
                mCities[position][Constants.CITY_ID]!!
            )
        }  // end of with(binding)

    }  // end of setBarangayValues method

    // Function to validate market input values
    private fun marketValidation(): Boolean {
        with(binding) {
            // Create a FormValidation object, and then execute the validations
            return FormValidation(this@MarketEditorActivity).run {
                when {
                    // Market Name
                    !validateName(etMktEditName) -> false
                    // Parent Wet Market Name
                    !validateName(etMktEditWetMkt) -> false
                    // Market Category
                    !checkSpinnerSelection(actvMktEditCategory) -> false
                    // Specify Category (Market Category must be Others)
                    actvMktEditCategory.text.toString()
                        .trim { it <= ' ' } == Constants.ITEM_OTHERS
                            && !validateSpecifyOthers(etMktEditOtherSpec) -> false
                    // Province
                    !checkSpinnerSelection(actvMktEditProvince) -> false
                    // City/Municipality
                    !checkSpinnerSelection(actvMktEditCtm) -> false
                    // Barangay
                    !checkSpinnerSelection(actvMktEditBrgy) -> false
                    // Postal Code
                    !validatePostalCode(etMktEditPostal) -> false
                    // Detailed Address
                    !validateLongTexts(etMktEditDetails) -> false
                    // Vendor's T&C (Vendor Registration only)
                    mMarketInfo == null && !requiredCheckbox(cbVendorTAndC) -> false
                    // When all fields are valid
                    else -> true
                }
            }  // end of run
        }  // end of with(binding)

    }  // end of marketValidation method

    // Function to proceed with saving Market Information
    internal fun saveMarketChanges() {
        // Validate first the market inputs
        if (marketValidation()) {
            /* Display the loading message. "Please wait..." - Vendor Registration.
             * "Saving changes..." - Edit Market.
             */
            showProgressDialog(
                this@MarketEditorActivity, this@MarketEditorActivity,
                if (mMarketInfo != null) getString(R.string.msg_saving_changes)
                else getString(R.string.msg_please_wait)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@MarketEditorActivity, mSelectedImageFileURI
                )
            }
            // If the user didn't upload the image
            else {
                // Just add or update the market info without market image
                addOrUpdateMarket()
            }  // end of if-else

        }  // end of if

    }  // end of saveMarketChanges method

    // Function to add (null mMarketInfo) or update market information in the Firestore
    internal fun addOrUpdateMarket(imageURL: String? = null) {
        with(binding) {
            // If mMarketInfo is null, add a new market
            if (mMarketInfo == null) {
                // Get the document reference for the new market
                val marketRef = FirestoreClass().getMarketReference()

                // Object to store market data
                mMarketInfo = Market(
                    Constants.MARKET_ID_TEMP + marketRef.id,
                    etMktEditName.text.toString().trim { it <= ' ' },
                    imageURL ?: "",
                    etMktEditWetMkt.text.toString().trim { it <= ' ' },
                    etMktEditDetails.text.toString().trim { it <= ' ' },
                    actvMktEditBrgy.text.toString().trim { it <= ' ' },
                    actvMktEditCtm.text.toString().trim { it <= ' ' },
                    actvMktEditProvince.text.toString().trim { it <= ' ' },
                    etMktEditPostal.text.toString().trim { it <= ' ' }.toInt(),
                    mCategoryPos,
                    etMktEditOtherSpec.text.toString().trim { it <= ' ' },
                    FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    Constants.getMarkerLocationData(mCurrentMarkerPos)
                )

                // Adds the market data in the Firestore database
                FirestoreClass().addMarket(this@MarketEditorActivity, mMarketInfo!!)
            }
            // If mMarketInfo has a value, update the current market
            else {
                // Overwrite market image URL, if the user uploaded the new image
                if (imageURL != null)
                    mMarketHashMap[Constants.IMAGE] = imageURL

                // Proceed to update fields in the Cloud Firestore
                FirestoreClass().updateMarket(
                    this@MarketEditorActivity, mMarketInfo!!.id, mMarketHashMap
                )
            }  // end of if-else

        }  // end of with(binding)

    }  // end of addOrUpdateMarket method

    // Function to store modified market information
    private fun storeMarketInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mMarketHashMap.clear()

        /* Store temporary object for comparison purposes if the page is for
         * vendor registration
         */
        if (mIsNewMarket) mMarketInfo = Market()

        with(binding) {
            mMarketInfo?.let { mkt ->
                val marketName = etMktEditName.text.toString().trim { it <= ' ' }
                // Save the new market name if it is different from previous market name
                if (marketName != mkt.name)
                    mMarketHashMap[Constants.NAME] = marketName

                val wetMarket = etMktEditWetMkt.text.toString().trim { it <= ' ' }
                /* Save the new parent wet market if it is different from previous
                 * parent wet market
                 */
                if (wetMarket != mkt.wetMarket)
                    mMarketHashMap[Constants.WET_MARKET] = wetMarket

                val province = actvMktEditProvince.text.toString().trim { it <= ' ' }
                // Save the new province if it is different from previous province
                if (province != mkt.province)
                    mMarketHashMap[Constants.PROVINCE] = province

                val city = actvMktEditCtm.text.toString().trim { it <= ' ' }
                /* Save the new city/municipality if it is different from previous
                 * city/municipality
                 */
                if (city != mkt.city)
                    mMarketHashMap[Constants.CITY] = city

                val barangay = actvMktEditBrgy.text.toString().trim { it <= ' ' }
                // Save the new barangay if it is different from previous barangay
                if (barangay != mkt.barangay)
                    mMarketHashMap[Constants.BARANGAY] = barangay

                val postalCode = if (etMktEditPostal.text!!.isNotEmpty())
                    etMktEditPostal.text.toString().trim { it <= ' ' }.toInt()
                else
                    0  // Default value to prevent NumberFormatException
                // Save the new postal code if it is different from previous postal code
                if (postalCode != mkt.postal)
                    mMarketHashMap[Constants.POSTAL] = postalCode

                val detailAdd = etMktEditDetails.text.toString().trim { it <= ' ' }
                /* Save the new detailed address if it is different
                 * from previous detailed address
                 */
                if (detailAdd != mkt.detailAdd)
                    mMarketHashMap[Constants.DETAIL_ADDR] = detailAdd

                val catCode = mCategoryPos
                // Save the new category code if it is different from previous category code
                if (catCode != mkt.category)
                    mMarketHashMap[Constants.CATEGORY] = catCode

                val otherCat = etMktEditOtherSpec.text.toString().trim { it <= ' ' }
                /* Save the new other category if it is different from previous
                 * other category
                 */
                if (otherCat != mkt.otherCat)
                    mMarketHashMap[Constants.OTHER_CATEGORY] = otherCat

                val addrLocation = if (mCurrentMarkerPos != null)
                    Constants.getMarkerLocationData(mCurrentMarkerPos)
                else
                    null  // Default value
                /* Save the new address location if it is different from
                 * the previous address location
                 */
                if (addrLocation != mkt.location)
                    mMarketHashMap[Constants.LOCATION] = addrLocation

                // Check if a user has changed the market image, add a temporary value
                if (mTempMarketImageURL != mkt.image)
                    mMarketHashMap[Constants.IMAGE] = mTempMarketImageURL
            }  // end of let
        }  // end of with(binding)

        // Revert back to null if the page is for vendor registration
        if (mIsNewMarket) mMarketInfo = null

    }  // end of storeMarketInfoChanges method

    // Function to prompt that the market was saved
    internal fun marketSavedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Variable to store the toast message, depending on the use case
        val message = if (mMarketInfo == null) {
            /* Show the message that the user has upgraded to vendor when
             * he/she registers.
             */
            getString(R.string.msg_user_now_vendor)
        } else {
            /* Show the message that the market's information has successfully
             * modified.
             */
            getString(R.string.msg_market_saved)
        }

        // Displays the Toast message
        toastMessage(this@MarketEditorActivity, message)

        finish()  // Closes the activity
    }  // end of marketSavedPrompt method

}  // end of MarketEditorActivity class
