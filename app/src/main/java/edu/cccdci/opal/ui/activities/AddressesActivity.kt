package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = ActivityAddressesBinding.inflate(layoutInflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Address>()
            // Gets all the documents from addresses collection
            .setQuery(FirestoreClass().getAddressQuery(), Address::class.java)
            .build()

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

            // Sets the layout type of the RecyclerView
            rvAddresses.layoutManager = WrapperLinearLayoutManager(
                this@AddressesActivity, LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Address Adapter
            addressAdapter = AddressAdapter(
                this@AddressesActivity, mSelectableAddress, options
            )
            // Sets the adapter of Address RecyclerView
            rvAddresses.adapter = addressAdapter

            // Actions when the Add Address button is clicked
            btnAddAddress.setOnClickListener {
                // Sends user to the Address Editor
                startActivity(
                    Intent(
                        this@AddressesActivity,
                        AddressEditActivity::class.java
                    )
                )
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

}  // end of AddressesActivity class