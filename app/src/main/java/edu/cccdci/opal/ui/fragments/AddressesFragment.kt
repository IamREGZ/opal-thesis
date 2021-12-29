package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.AddressAdapter
import edu.cccdci.opal.databinding.FragmentAddressesBinding
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager

class AddressesFragment : Fragment() {

    private lateinit var binding: FragmentAddressesBinding
    private var addressAdapter: AddressAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddressesBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Address>()
            // Gets all the documents from addresses collection
            .setQuery(FirestoreClass().getAddressQuery(), Address::class.java)
            .build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvAddresses.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Address Adapter
            addressAdapter = AddressAdapter(
                this@AddressesFragment, requireContext(), options
            )
            // Sets the adapter of Address RecyclerView
            rvAddresses.adapter = addressAdapter

            // Actions when the Add Address button is clicked
            btnAddAddress.setOnClickListener {
                // Sends user to the Address Editor
                findNavController().navigate(R.id.addresses_to_address_info)
            }

            return root
        }
    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on addresses
        addressAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onStop() {
        super.onStop()

        // Stops listening to Firestore operations on addresses
        if (addressAdapter != null)
            addressAdapter!!.stopListening()
    }  // end of onStop method

}  // end of AddressesFragment class