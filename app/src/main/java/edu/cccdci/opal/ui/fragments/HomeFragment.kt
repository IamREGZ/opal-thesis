package edu.cccdci.opal.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.MarketBrowseAdapter
import edu.cccdci.opal.adapters.MarketCategoriesAdapter
import edu.cccdci.opal.databinding.FragmentHomeBinding
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager
import edu.cccdci.opal.ui.activities.AddressesActivity
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.utils.Constants

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var browseAdapter: MarketBrowseAdapter
    private lateinit var categoriesAdapter: MarketCategoriesAdapter
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private var mUserInfo: User? = null
    private var mUserCurrentLocation: CurrentLocation? = null
    private var mCurLocJson: String = ""
    private var mSelectedLocAddress: Address? = null
    // private var productAdapter: ProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is important to make two icons (my cart and messages) visible
        setHasOptionsMenu(true)
    }  // end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        // Creates the Shared Preferences
        mSharedPrefs = requireContext().getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        mCurLocJson = mSharedPrefs.getString(
            Constants.CURRENT_LOCATION, ""
        )!!

        val selectedLocation = mSharedPrefs.getString(
            Constants.CURRENT_ADDRESS_DETAILS, ""
        )!!

        if (selectedLocation.isNotEmpty()) {
            mSelectedLocAddress = Gson().fromJson(
                selectedLocation, Address::class.java
            )
        }

        // Create a Builder for FirestoreRecyclerOptions
//        val options = FirestoreRecyclerOptions.Builder<Product>()
//            // Gets all the documents from products collection
//            .setQuery(
//                FirestoreClass().getProductQuery(this@HomeFragment),
//                Product::class.java
//            )
//            .build()

        setLocationSpinner()  // Set the location options spinner items

        with(binding) {
            // Sets the layout type of the RecyclerView
//            rvHome.layoutManager = WrapperGridLayoutManager(
//                requireContext(), 2, GridLayoutManager.VERTICAL, false
//            )

            // Create an object of Product Adapter
//            productAdapter = ProductAdapter(
//                requireContext(), this@HomeFragment, options
//            )

            // Sets the adapter of Home RecyclerView
//            rvHome.adapter = productAdapter

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Get the argument from the parent activity (MainActivity)
        val bundle = this.arguments

        /* Get the parcelable class (User) from the parent activity
         * (MainActivity)
         */
        if (bundle != null)
            mUserInfo = bundle.getParcelable(Constants.EXTRA_USER_INFO)

        // Starts listening to Firestore operations on products
        // productAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onDestroyView() {
        super.onDestroyView()

        // Stops listening to Firestore operations on products
//        if (productAdapter != null)
//            productAdapter!!.stopListening()
    }  // end of onDestroyView method

    // Override the function to add two icons on top app bar (my cart and messages)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }  // end of onCreateOptionsMenu method

    // onOptionsItemSelected events are declared here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Send to Item Cart
            R.id.tab_cart -> {
                // Create an Intent to launch CartActivity
                val intent = Intent(requireContext(), CartActivity::class.java)
                // Add product information to intent
                intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                // Opens the user cart activity
                requireContext().startActivity(intent)
            }

            // Send to Messages
            R.id.tab_messages -> findNavController().navigate(R.id.home_to_messages)
        }

        return super.onOptionsItemSelected(item)
    }  // end of onOptionsItemSelected method

    // Function to set the RecyclerView adapters of Browse and Categories
    private fun setCategoryPanels() {
        with(binding) {
            // Sets the layout attributes of Market Browse RecyclerView
            rvMarketBrowse.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

            // Sets the layout attributes of Market Categories RecyclerView
            rvMarketCategories.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )

            // Create an object of Market Browse Adapter
            browseAdapter = MarketBrowseAdapter(
                requireContext(),
                arrayOf(
                    mUserCurrentLocation!!.latitude,
                    mUserCurrentLocation!!.longitude
                ),
                resources.getStringArray(R.array.browse_market_categories)
            )
            // Create an object of Market Categories Adapter
            categoriesAdapter = MarketCategoriesAdapter(
                requireContext(),
                arrayOf(
                    mUserCurrentLocation!!.latitude,
                    mUserCurrentLocation!!.longitude
                ),
                resources.getStringArray(R.array.market_categories_list)
            )

            // Sets the adapter of Market Browse RecyclerView
            rvMarketBrowse.adapter = browseAdapter
            // Sets the adapter of Market Categories RecyclerView
            rvMarketCategories.adapter = categoriesAdapter
        }  // end of with(binding)

    }  // end of setBrowseCategories method

    // Function to set the spinner items for location options
    private fun setLocationSpinner() {
        val locationOptions = resources
            .getStringArray(R.array.location_spinner_options)

        with(binding) {
            // Prepare the drop down values for location options
            val locOptsAdapter = ArrayAdapter(
                requireContext(), R.layout.spinner_item,
                locationOptions
            )

            if (mCurLocJson.isNotEmpty()) {
                mUserCurrentLocation = Gson().fromJson(
                    mCurLocJson, CurrentLocation::class.java
                )

                if (mSelectedLocAddress != null) {
                    mUserCurrentLocation!!.code = 1
                    mUserCurrentLocation!!.latitude = mSelectedLocAddress!!.location!!.latitude
                    mUserCurrentLocation!!.longitude = mSelectedLocAddress!!.location!!.longitude
                }

                mSPEditor.putString(
                    Constants.CURRENT_LOCATION, Gson().toJson(mUserCurrentLocation)
                ).apply()
            }

            actvCurrentLocation.setText(
                if (mUserCurrentLocation!!.code >= 0)
                    locationOptions[mUserCurrentLocation!!.code]
                else
                    ""
            )

            actvCurrentLocation.setAdapter(locOptsAdapter)

            actvCurrentLocation.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {}
                    1 -> {
                        // Create an intent to launch AddressesActivity
                        val intent = Intent(
                            requireContext(), AddressesActivity::class.java
                        )
                        // To enable address selection functionality in the target activity
                        intent.putExtra(Constants.SELECTABLE_ENABLED, true)
                        intent.putExtra(Constants.SELECTION_MODE, 1)

                        startActivity(intent)  // Opens the activity
                    }
                }
            }
        }

        setCategoryPanels()  // Set the category panels in the home page
    }  // end of setLocationSpinner method

}  // end of HomeFragment class