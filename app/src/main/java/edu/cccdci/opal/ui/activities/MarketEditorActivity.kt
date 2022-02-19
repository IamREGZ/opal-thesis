package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMarketEditorBinding
import edu.cccdci.opal.dataclasses.Location
import edu.cccdci.opal.dataclasses.Market
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass
import java.io.IOException
import java.util.*

class MarketEditorActivity : UtilityClass(), View.OnClickListener,
    OnMapReadyCallback {

    private lateinit var binding: ActivityMarketEditorBinding
    private lateinit var mCategoryList: Array<String>
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private val mProvinces: MutableList<HashMap<String, String>> = mutableListOf()
    private val mProvNames: MutableList<String> = mutableListOf()
    private val mCities: MutableList<HashMap<String, String>> = mutableListOf()
    private val mCTNames: MutableList<String> = mutableListOf()
    private var mSelectedProvince: String = ""
    private var mMarketInfo: Market? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mMarketImageURL: String = ""
    private var mCategoryPos: Int = 0
    private var mCurrentMarkerPos: LatLng? = null
    private var mMarketHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMarketEditorBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        with(binding) {
            setContentView(binding.root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMarketEditActivity, false)

            // Get the string array of market categories
            mCategoryList = resources.getStringArray(
                R.array.market_categories_list
            )

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.MARKET_INFO)) {
                // Get the parcelable data from intent
                mMarketInfo = intent.getParcelableExtra(Constants.MARKET_INFO)

                // Set the top action bar title to "EDIT MARKET"
                tvMarketEditTitle.text = getString(R.string.tlb_title_edit_market)
                // Make the Vendor Registration head visibility gone
                llVendorRegPanel.visibility = View.GONE

                // Make the Vendor T&C Checkbox visibility gone
                fblVendorTAndC.visibility = View.GONE
                cbVendorTAndC.isChecked = true
                cbVendorTAndC.isEnabled = false
            }  // end of if

            // Store the market values if it is not null (prevents NPE)
            if (mMarketInfo != null) {
                setUserMarketValues()
            } else {
                /* Set the Shared Preference of current marker position blank
                 * whenever a user registers for wet market.
                 */
                mSPEditor.putString(Constants.CURRENT_MARKER_POS, "").apply()
            }

            // Prepare the SupportMapFragment
            mSupportMap = supportFragmentManager
                .findFragmentById(R.id.mpfr_market_address_map) as SupportMapFragment
            // Load the map fragment
            mSupportMap.getMapAsync(this@MarketEditorActivity)

            // Prepare the drop down values for market categories
            val categoryAdapter = ArrayAdapter(
                this@MarketEditorActivity, R.layout.spinner_item,
                mCategoryList
            )
            actvMktEditCategory.setAdapter(categoryAdapter)

            // Call the Firestore Function to retrieve province data
            FirestoreClass().getProvinces(this@MarketEditorActivity)

            // Prepare the drop down values for provinces
            val provinceAdapter = ArrayAdapter(
                this@MarketEditorActivity, R.layout.spinner_item, mProvNames
            )
            actvMktEditProvince.setAdapter(provinceAdapter)

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
                // Store the current position of selected market category item
                mCategoryPos = position

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
            }

            // Click event for Market Image Selector Layout
            llMarketImageSelector.setOnClickListener(this@MarketEditorActivity)
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
                R.id.btn_submit_market -> saveMarketChanges()

            }  // end of when

        }  // end of if

    }  // end of onClick method

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
                    resources.getString(R.string.err_permission_denied),
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
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@MarketEditorActivity,
                    resources.getString(R.string.err_image_selection_failed)
                )
            }  // end of try-catch

        }  // end of if

    }  // end of onActivityResult method

    // Overriding function to set the Map UI of market's location
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        mGoogleMap.apply {
            clear()  // Clear all the markers set in the map

            val markerPosition = if (mCurrentMarkerPos != null) {
                // Set the marker position to the current position if it is available
                mCurrentMarkerPos!!
            } else {
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
                    this@MarketEditorActivity, MapActivity::class.java
                )

                // Add the marker position to the intent
                intent.putExtra(
                    Constants.CURRENT_MARKER_POS, markerPosition
                )

                startActivity(intent)  // Opens the Map Activity
            }
        }  // end of apply

    }  // end of onMapReady method

    // Function to store existing user's market data in the respective fields
    private fun setUserMarketValues() {
        with(binding) {
            // Market Name and Parent Wet Market
            etMktEditName.setText(mMarketInfo!!.name)
            etMktEditWetMkt.setText(mMarketInfo!!.wetMarket)

            // Set the category position to the current category code
            mCategoryPos = mMarketInfo!!.category
            // Then get the text from category list array and set its value
            actvMktEditCategory.setText(mCategoryList[mCategoryPos])

            /* If the category is "Others," set the Specify Others field
             * with the specified other category.
             */
            if (mCategoryList[mCategoryPos] == Constants.ITEM_OTHERS) {
                tilMktEditOtherSpec.visibility = View.VISIBLE
                etMktEditOtherSpec.setText(mMarketInfo!!.otherCat)
            }

            // Market Address
            actvMktEditProvince.setText(mMarketInfo!!.province)
            actvMktEditCtm.setText(mMarketInfo!!.city)
            actvMktEditBrgy.setText(mMarketInfo!!.barangay)
            etMktEditPostal.setText(mMarketInfo!!.postal.toString())
            etMktEditDetails.setText(mMarketInfo!!.detailAdd)

            // Load the market image
            GlideLoader(this@MarketEditorActivity).loadImage(
                mMarketInfo!!.image, ivVenMarketImage
            )

            // Set the current marker position to the existing user location
            mSPEditor.putString(
                Constants.CURRENT_MARKER_POS,
                // Prevents NullPointerException
                if (mMarketInfo!!.location != null) {
                    // Update the current marker position
                    mCurrentMarkerPos = LatLng(
                        mMarketInfo!!.location!!.latitude,
                        mMarketInfo!!.location!!.longitude
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
        }  // end of with(binding)

    }  // end of setUserMarketValues method

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
            actvMktEditCtm.text.clear()

            // Call the Firestore function to retrieve city data
            FirestoreClass().getCities(
                this@MarketEditorActivity, mSelectedProvince
            )

            // Prepare the drop down values for cities
            val cityAdapter = ArrayAdapter(
                this@MarketEditorActivity, R.layout.spinner_item, mCTNames
            )
            actvMktEditCtm.setAdapter(cityAdapter)

            // Enable drop down functionality of city/municipality
            if (!actvMktEditCtm.isEnabled) {
                tilMktEditCtm.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvMktEditCtm.isEnabled = true
            }

            // Disable drop down functionality of barangays
            if (actvMktEditBrgy.isEnabled) {
                tilMktEditBrgy.endIconMode = TextInputLayout.END_ICON_NONE
                actvMktEditBrgy.isEnabled = false
            }

            // Clear the drop down data of barangay for a new batch of data later
            actvMktEditBrgy.text.clear()
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
            if (actvMktEditBrgy.isEnabled) actvMktEditBrgy.text.clear()

            // Call the Firestore function to retrieve barangay data
            val brgyResult = FirestoreClass().getBarangays(
                this@MarketEditorActivity, mSelectedProvince,
                mCities[position][Constants.CITY_ID]!!
            )

            // Prepare the drop down values for barangays
            val brgyAdapter = ArrayAdapter(
                this@MarketEditorActivity, R.layout.spinner_item, brgyResult
            )
            actvMktEditBrgy.setAdapter(brgyAdapter)

            // Enable the drop down functionality of barangays
            if (!actvMktEditBrgy.isEnabled) {
                tilMktEditBrgy.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvMktEditBrgy.isEnabled = true
            }
        }  // end of with(binding)
    }  // end of setBarangayValues method

    // Function to validate market input values
    private fun marketValidation(): Boolean {
        with(binding) {
            return when {
                // If the Market Name field is empty
                TextUtils.isEmpty(etMktEditName.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_market_name),
                        true
                    )
                    false  // return false
                }

                // If the Parent Wet Market field is empty
                TextUtils.isEmpty(etMktEditWetMkt.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_wet_market),
                        true
                    )
                    false  // return false
                }

                // If no Market Category is selected
                TextUtils.isEmpty(actvMktEditCategory.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_no_mkt_category_selected),
                        true
                    )
                    false  // return false
                }

                /* If the user selects Others in the Market Category and
                 * the Custom Category field is empty
                 */
                actvMktEditCategory.text.toString()
                    .trim { it <= ' ' } == Constants.ITEM_OTHERS &&
                        TextUtils.isEmpty(etMktEditOtherSpec.text.toString()
                            .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_custom_category),
                        true
                    )
                    false  // return false
                }

                // If no province is selected
                TextUtils.isEmpty(actvMktEditProvince.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_mkt_province),
                        true
                    )
                    false  // return false
                }

                // If no city/municipality is selected
                TextUtils.isEmpty(actvMktEditCtm.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_mkt_city),
                        true
                    )
                    false  // return false
                }

                // If no barangay is selected
                TextUtils.isEmpty(actvMktEditBrgy.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_mkt_brgy),
                        true
                    )
                    false  // return false
                }

                // If the Postal Code field is empty
                TextUtils.isEmpty(etMktEditPostal.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_mkt_postal),
                        true
                    )
                    false  // return false
                }

                // If the Detailed Address field is empty
                TextUtils.isEmpty(etMktEditDetails.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_blank_mkt_detailed),
                        true
                    )
                    false  // return false
                }

                /* If the Vendor's T&C checkbox is not checked
                 * (null mMarketInfo means that the user is registering
                 * for vendor).
                 */
                mMarketInfo == null && !cbVendorTAndC.isChecked -> {
                    // Display an error message
                    showSnackBar(
                        this@MarketEditorActivity,
                        getString(R.string.err_unchecked_vendor_tac),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all inputs are valid

            }  // end of when

        }  // end of with(binding)

    }  // end of marketValidation method

    // Function to proceed with saving Market Information
    private fun saveMarketChanges() {
        // Validate first the market inputs
        if (marketValidation()) {
            // Display the loading message
            showProgressDialog(
                this@MarketEditorActivity, this@MarketEditorActivity,
                getString(R.string.msg_please_wait)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@MarketEditorActivity, mSelectedImageFileURI
                )
            } else {
                // If the user didn't upload the image
                if (mMarketInfo == null) {
                    // Add market data if mMarketInfo is null
                    addMarketInfo()
                } else {
                    // Update market data if mMarketInfo contains any information
                    updateMarketInfo()
                }

            }  // end of if-else

        }  // end of if

    }  // end of saveMarketChanges method

    /* Function to prompt that the user has successfully uploaded the image.
     * And then adds the rest of the market information.
     */
    fun imageUploadSuccess(imageURL: String) {
        // Store the image URL of Product Image
        mMarketImageURL = imageURL

        if (mMarketInfo == null) {
            // Proceed to add the rest of the market information if mMarketInfo is null
            addMarketInfo()
        } else {
            /* Proceed to update the rest of the market information if
             * mMarketInfo contains any information
             */
            updateMarketInfo()
        }  // end of if-else

    }  // end of imageUploadSuccess method

    // Function to add market information in the Firestore
    private fun addMarketInfo() {
        with(binding) {
            // Get the document reference for the new market
            val marketRef = FirestoreClass().getMarketReference()

            // Object to store market data
            val market = Market(
                Constants.MARKET_ID_TEMP + marketRef.id,
                etMktEditName.text.toString().trim { it <= ' ' },
                mMarketImageURL,
                etMktEditWetMkt.text.toString().trim { it <= ' ' },
                etMktEditDetails.text.toString().trim { it <= ' ' },
                actvMktEditBrgy.text.toString().trim { it <= ' ' },
                actvMktEditCtm.text.toString().trim { it <= ' ' },
                actvMktEditProvince.text.toString().trim { it <= ' ' },
                etMktEditPostal.text.toString().trim { it <= ' ' }.toInt(),
                mCategoryPos,
                etMktEditOtherSpec.text.toString().trim { it <= ' ' },
                FirebaseAuth.getInstance().currentUser?.uid ?: "",
                Location(
                    mCurrentMarkerPos!!.latitude,
                    mCurrentMarkerPos!!.longitude,
                    GeoFireUtils.getGeoHashForLocation(
                        GeoLocation(
                            mCurrentMarkerPos!!.latitude,
                            mCurrentMarkerPos!!.longitude,
                        )
                    )
                )
            )

            // Adds the market data in the Firestore database
            FirestoreClass().addMarket(this@MarketEditorActivity, market)
        }  // end of with(binding)

    }  // end of addMarketInfo method

    // Function to store modified information in the Firestore (if any)
    private fun updateMarketInfo() {
        storeMarketInfoChanges()  // Stores modified information, if any

        // Overwrite market image URL, if the user uploaded the image
        if (mMarketImageURL.isNotEmpty())
            mMarketHashMap[Constants.IMAGE] = mMarketImageURL

        // Proceed to update fields in the Cloud Firestore
        FirestoreClass().updateMarket(
            this@MarketEditorActivity, mMarketInfo!!.id, mMarketHashMap
        )
    }  // end of updateMarketInfo method

    // Function to store modified market information
    private fun storeMarketInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mMarketHashMap.clear()

        with(binding) {
            val marketName = etMktEditName.text.toString().trim { it <= ' ' }
            // Save the new market name if it is different from previous market name
            if (marketName != mMarketInfo!!.name)
                mMarketHashMap[Constants.NAME] = marketName

            val wetMarket = etMktEditWetMkt.text.toString().trim { it <= ' ' }
            /* Save the new parent wet market if it is different from previous
             * parent wet market
             */
            if (wetMarket != mMarketInfo!!.wetMarket)
                mMarketHashMap[Constants.WET_MARKET] = wetMarket

            val province = actvMktEditProvince.text.toString().trim { it <= ' ' }
            // Save the new province if it is different from previous province
            if (province != mMarketInfo!!.province)
                mMarketHashMap[Constants.PROVINCE] = province

            val city = actvMktEditCtm.text.toString().trim { it <= ' ' }
            /* Save the new city/municipality if it is different from previous
             * city/municipality
             */
            if (city != mMarketInfo!!.city)
                mMarketHashMap[Constants.CITY] = city

            val barangay = actvMktEditBrgy.text.toString().trim { it <= ' ' }
            // Save the new barangay if it is different from previous barangay
            if (barangay != mMarketInfo!!.barangay)
                mMarketHashMap[Constants.BARANGAY] = barangay

            val postalCode = etMktEditPostal.text.toString().trim { it <= ' ' }.toInt()
            // Save the new postal code if it is different from previous postal code
            if (postalCode != mMarketInfo!!.postal)
                mMarketHashMap[Constants.POSTAL] = postalCode

            val detailAdd = etMktEditDetails.text.toString().trim { it <= ' ' }
            /* Save the new detailed address if it is different
             * from previous detailed address
             */
            if (detailAdd != mMarketInfo!!.detailAdd)
                mMarketHashMap[Constants.DETAIL_ADDR] = detailAdd

            val catCode = mCategoryPos
            // Save the new category code if it is different from previous category code
            if (catCode != mMarketInfo!!.category)
                mMarketHashMap[Constants.CATEGORY] = catCode

            val otherCat = etMktEditOtherSpec.text.toString().trim { it <= ' ' }
            /* Save the new other category if it is different from previous
             * other category
             */
            if (otherCat != mMarketInfo!!.otherCat)
                mMarketHashMap[Constants.OTHER_CATEGORY] = otherCat

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
            if (addrLocation != mMarketInfo!!.location!!)
                mMarketHashMap[Constants.LOCATION] = addrLocation
        }  // end of with(binding)
    }  // end of storeMarketInfoChanges method

    // Function to prompt that the market was saved
    fun marketSavedPrompt() {
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