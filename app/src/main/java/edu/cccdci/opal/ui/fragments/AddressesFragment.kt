package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentAddressesBinding

class AddressesFragment : Fragment() {

    private lateinit var binding: FragmentAddressesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is important to make add icon visible
        setHasOptionsMenu(true)
    }  // end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddressesBinding.inflate(inflater)
        return binding.root
    }  // end of onCreateView method

    // Override the function to add plus icon on top app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plus_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }  // end of onCreateOptionsMenu method

    // onOptionsItemSelected events are declared here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Go to Add Address
            R.id.tab_add -> findNavController().navigate(R.id.addresses_to_address_info)
        }

        return super.onOptionsItemSelected(item)
    }  // end of onOptionsItemSelected method

}  // end of AddressesFragment class