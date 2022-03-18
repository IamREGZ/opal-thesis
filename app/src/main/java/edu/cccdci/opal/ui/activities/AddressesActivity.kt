package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.AddressAdapter
import edu.cccdci.opal.databinding.ActivityAddressesBinding
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class AddressesActivity : UtilityClass() {

    private lateinit var binding: ActivityAddressesBinding
    private var addressAdapter: AddressAdapter? = null
    // Enables selection of address when it is true
    private var mSelectableAddress: Boolean = false
    private var mMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityAddressesBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbAddressesActivity, false)

            // Check if there's an existing boolean info
            if (intent.hasExtra(Constants.SELECTABLE_ENABLED)) {
                // Get the Boolean value (default value is false)
                mSelectableAddress = intent.getBooleanExtra(
                    Constants.SELECTABLE_ENABLED, false
                )
                // Change the toolbar title
                tvAddressesTitle.text = resources.getString(
                    R.string.tlb_title_select_address
                )
            }

            // Might be temporary
            if (intent.hasExtra(Constants.SELECTION_MODE)) {
                mMode = intent.getIntExtra(
                    Constants.SELECTION_MODE, 0
                )
            }

            setupAddressAdapter()  // Setup the Address RecyclerView Adapter

            // Actions when the Add Address button is clicked
            btnAddAddress.setOnClickListener {
                // Maximum allowed number of addresses is five (5)
                if (addressAdapter!!.itemCount in 0..4) {
                    // Sends user to the Address Editor
                    startActivity(
                        Intent(
                            this@AddressesActivity,
                            AddressEditActivity::class.java
                        )
                    )
                } else {
                    // If the number of addresses is exactly 5, display an message
                    showSnackBar(
                        this@AddressesActivity,
                        getString(R.string.err_address_limit),
                        true
                    )
                }
            }
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible
    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on addresses
        addressAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when this activity is no longer visible
    override fun onStop() {
        super.onStop()

        // Stops listening to Firestore operations on addresses
        if (addressAdapter != null)
            addressAdapter!!.stopListening()
    }  // end of onStop method

    // Function to setup the RecyclerView adapter for addresses
    private fun setupAddressAdapter() {
        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Address>()
            // Gets all the documents from addresses collection
            .setQuery(FirestoreClass().getAddressQuery(), Address::class.java)
            .build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvAddresses.layoutManager = WrapperLinearLayoutManager(
                this@AddressesActivity, LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Address Adapter
            addressAdapter = AddressAdapter(
                this@AddressesActivity, mSelectableAddress, mMode ?: -1, options
            )
            // Sets the adapter of Address RecyclerView
            rvAddresses.adapter = addressAdapter
        }  // end of with(binding)

    }  // end of setupAddressAdapter method

}  // end of AddressesActivity class
