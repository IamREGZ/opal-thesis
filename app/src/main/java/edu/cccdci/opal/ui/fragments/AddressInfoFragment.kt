package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentAddressInfoBinding
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class AddressInfoFragment : Fragment() {

    private lateinit var binding: FragmentAddressInfoBinding
    private lateinit var mUtility: UtilityClass
    private val mProvinces: MutableList<HashMap<String, String>> = mutableListOf()
    private val mProvNames: MutableList<String> = mutableListOf()
    private val mCities: MutableList<HashMap<String, String>> = mutableListOf()
    private val mCTNames: MutableList<String> = mutableListOf()
    private var mSelectedProvince: String = ""
    private var mAddress: Address? = null
    private var mAddrHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddressInfoBinding.inflate(inflater)

        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        // Get the bundle from the previous fragment (AddressesFragment)
        val bundle = this.arguments

        /* Get the parcelable class (Address) from the previous
         * fragment (Addresses Fragment)
         */
        if (bundle != null)
            mAddress = bundle.getParcelable(Constants.USER_ADDRESS)

        with(binding) {
            // Store the address data values if it has parcelable class
            if (mAddress != null)
                setSelectedAddressValues()

            // Call the Firestore Function to retrieve province data
            FirestoreClass().getProvinces(this@AddressInfoFragment)

            // Prepare the drop down values for provinces
            val provinceAdapter = ArrayAdapter(
                requireContext(), R.layout.spinner_item, mProvNames
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

            // Actions when the submit button is clicked
            btnSubmitAddress.setOnClickListener {
                saveUserAddress()
            }

            // Actions when the delete button is clicked
            btnDeleteAddress.setOnClickListener {
                deleteUserAddress()
            }

            return root
        }  // end of with(binding)
    }  // end of onCreateView method

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
            smPickupAddress.isChecked = mAddress!!.pickup

            // Change the interface of Address Info
            tvAddrInfoHead.setText(R.string.edit_address_head)
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
                this@AddressInfoFragment, mSelectedProvince
            )

            // Prepare the drop down values for cities
            val cityAdapter = ArrayAdapter(
                requireContext(), R.layout.spinner_item, mCTNames
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
            if (actvAddrBrgy.isEnabled)
                actvAddrBrgy.text.clear()

            // Call the Firestore function to retrieve barangay data
            val brgyResult = FirestoreClass().getBarangays(
                requireContext(),
                mSelectedProvince,
                mCities[position][Constants.CITY_ID]!!
            )

            // Prepare the drop down values for barangays
            val brgyAdapter = ArrayAdapter(
                requireContext(), R.layout.spinner_item, brgyResult
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
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_fullname),
                        true
                    )
                    false
                }

                // If the Phone Number field is empty
                TextUtils.isEmpty(etAddrPhone.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_phone),
                        true
                    )
                    false
                }

                // If no province is selected
                TextUtils.isEmpty(actvAddrProvince.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_province),
                        true
                    )
                    false
                }

                // If no city/municipality is selected
                TextUtils.isEmpty(actvAddrCtm.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_city),
                        true
                    )
                    false
                }

                // If no barangay is selected
                TextUtils.isEmpty(actvAddrBrgy.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_brgy),
                        true
                    )
                    false
                }

                // If the Postal Code field is empty
                TextUtils.isEmpty(etAddrPostal.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_postal),
                        true
                    )
                    false
                }

                // If the Detailed Address field is empty
                TextUtils.isEmpty(etAddrDetails.text.toString()
                    .trim { it <= ' ' }) -> {
                    mUtility.showSnackBar(
                        requireActivity(),
                        resources.getString(R.string.err_blank_detailed),
                        true
                    )
                    false
                }

                else -> true  // If all inputs are valid
            }  // end of when

        }  // end of with(binding)
    }  // end of addressValidation method

    // Function to save user's address
    private fun saveUserAddress() {
        with(binding) {
            // Validate first the address fields
            if (addressValidation()) {
                // Display the loading message
                mUtility.showProgressDialog(
                    requireContext(), requireActivity(),
                    resources.getString(R.string.msg_please_wait)
                )

                if (mAddress == null) {
                    // If mAddress object is null, add a new address

                    // Get the user id of the current user
                    val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    // Get the document reference for the new address
                    val addressRef: DocumentReference = FirebaseFirestore.getInstance()
                        .collection(Constants.USERS).document(userID)
                        .collection(Constants.ADDRESSES).document()

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
                        smPickupAddress.isChecked
                    )

                    // Adds the user address data in the Firestore Database
                    FirestoreClass().addUserAddress(
                        this@AddressInfoFragment, mAddress!!, mUtility
                    )
                } else {
                    // If mAddress has a value, update the current address

                    storeUserAddressChanges()  // Stores modified information, if any

                    // Proceed to update fields in the Cloud Firestore
                    FirestoreClass().updateAddress(
                        this@AddressInfoFragment, mAddress!!.addressID,
                        mAddrHashMap, mUtility
                    )
                }  // end of if-else
            }  // end of if

        }  // end of with(binding)

    }  // end of saveUserAddress method

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

            val pickupAdd = smPickupAddress.isChecked
            /* Save the new pickup address toggle if it is
             * the opposite of the previous toggle
             */
            if (pickupAdd != mAddress!!.pickup)
                mAddrHashMap[Constants.PICKUP_ADDR] = pickupAdd
        }  // end of with(binding)
    }  // end of storeUserAddressChanges method

    // Function to prompt user that the address is saved
    fun addressSavedPrompt() {
        mUtility.hideProgressDialog()  // Hide the loading message

        // Display a Toast message
        mUtility.toastMessage(
            requireContext(),
            resources.getString(R.string.msg_address_saved)
        )

        // Sends the user back to the previous fragment
        requireActivity().onBackPressed()
    }  // end of addressSavedPrompt method

    // Function to delete user's address
    private fun deleteUserAddress() {
        // Check if the mSelectedAddress object is not null
        if (mAddress != null) {
            // Display the loading message
            mUtility.showProgressDialog(
                requireContext(), requireActivity(),
                resources.getString(R.string.msg_please_wait)
            )

            // Deletes the user address data in the Firestore Database
            FirestoreClass().deleteAddress(
                this@AddressInfoFragment, mAddress!!.addressID,
                mUtility
            )
        }  // end of if
    }  // end of deleteUserAddress method

    // Function to prompt user that the address was deleted
    fun addressDeletedPrompt() {
        mUtility.hideProgressDialog()  // Hide the loading message

        // Display a Toast message
        mUtility.toastMessage(
            requireContext(), resources.getString(R.string.msg_address_deleted)
        )

        // Sends the user back to the previous fragment
        requireActivity().onBackPressed()
    }  // end of addressDeletedPrompt method

}  // end of AddressInfoFragment class