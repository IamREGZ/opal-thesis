package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentAddressInfoBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddressInfoBinding.inflate(inflater)

        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        // Call the Firestore Function to retrieve province data
        FirestoreClass().getProvinces(this@AddressInfoFragment)

        with(binding) {
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
                addressValidation()
            }

            return root
        }  // end of with(binding)
    }  // end of onCreateView method

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
            // Get the sorted list of Provinces
            val sortedProvinces = prvList.sortedWith(compareBy {
                it[Constants.PROVINCE_NAME]
            })
            // Add the whole sorted list of Provinces
            mProvinces.addAll(sortedProvinces)

            // Also, store the province names in the another list
            for (prov in mProvinces)
                mProvNames.add(prov[Constants.PROVINCE_NAME]!!)
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
            // Get the sorted list of Cities
            val sortedCities = ctList.sortedWith(compareBy {
                it[Constants.CITY_NAME]
            })
            // Add the whole sorted list of Cities
            mCities.addAll(sortedCities)

            // Also, store the city names in the another list
            for (ct in mCities)
                mCTNames.add(ct[Constants.CITY_NAME]!!)
        }
    }  // end of retrieveCities method

    /* Function to set drop down data of city/municipality once
     * the user selects an item from province.
     */
    private fun setCityValues(position: Int) {
        with(binding) {
            // Store the selected province's ID
            mSelectedProvince = mProvinces[position][Constants.PROVINCE_ID]!!

            // Enable drop down functionality of city/municipality
            if (!actvAddrCtm.isEnabled) {
                tilAddrCtm.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrCtm.isEnabled = true
            }
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

            // Disable drop down functionality of barangays
            if (actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_NONE
                actvAddrBrgy.isEnabled = false
                actvAddrBrgy.text.clear()
            }
        }  // end of with(binding)
    }  // end of setCityValues method

    /* Function to set drop down data of barangay once the user
     * selects an item from city.
     */
    private fun setBarangayValues(position: Int) {
        with(binding) {
            // Clear the drop down data of barangay for a new batch of data
            actvAddrBrgy.text.clear()

            // Call the Firestore function to retrieve barangay data
            val barangays = FirestoreClass().getBarangays(
                requireContext(),
                mSelectedProvince,
                mCities[position][Constants.CITY_ID]!!
            )

            // Prepare the drop down values for barangays
            val brgyAdapter = ArrayAdapter(
                requireContext(), R.layout.spinner_item, barangays
            )
            actvAddrBrgy.setAdapter(brgyAdapter)

            // Enable the drop down functionality of barangays
            if (!actvAddrBrgy.isEnabled) {
                tilAddrBrgy.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                actvAddrBrgy.isEnabled = true
            }
        }  // end of with(binding)
    }  // end of setBarangayValues method

    // TODO: Make the function return Boolean value
    // Function to validate user address
    private fun addressValidation() {
        with(binding) {
            when {
                // If the Full Name field is empty
                TextUtils.isEmpty(etAddrFullName.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_fullname),
                    true
                )

                // If the Phone Number field is empty
                TextUtils.isEmpty(etAddrPhone.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_phone),
                    true
                )

                // If no province is selected
                TextUtils.isEmpty(actvAddrProvince.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_province),
                    true
                )

                // If no city/municipality is selected
                TextUtils.isEmpty(actvAddrCtm.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_city),
                    true
                )

                // If no barangay is selected
                TextUtils.isEmpty(actvAddrBrgy.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_brgy),
                    true
                )

                // If the Postal Code field is empty
                TextUtils.isEmpty(etAddrPostal.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_postal),
                    true
                )

                // If the Detailed Address field is empty
                TextUtils.isEmpty(etAddrDetails.text.toString()
                    .trim { it <= ' ' }) -> mUtility.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.err_blank_detailed),
                    true
                )

                // If all inputs are valid
                else -> mUtility.showSnackBar(
                    requireActivity(), "All fields are valid.", false
                )
            }  // end of when

        }  // end of with(binding)
    }  // end of addressValidation method

}  // end of AddressInfoFragment class