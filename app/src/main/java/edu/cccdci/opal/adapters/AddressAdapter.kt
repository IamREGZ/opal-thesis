package edu.cccdci.opal.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.ui.fragments.AddressInfoFragment
import edu.cccdci.opal.utils.Constants

class AddressAdapter(
    private val fragment: Fragment,
    private val context: Context,
    options: FirestoreRecyclerOptions<Address>
): FirestoreRecyclerAdapter<Address, AddressAdapter.AddressViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from address item layout
        private val addrFullName: TextView = itemView.findViewById(R.id.tv_addr_full_name)
        private val phoneNum: TextView = itemView.findViewById(R.id.tv_addr_phone)
        private val addrLine1: TextView = itemView.findViewById(R.id.tv_addr_line_1)
        private val addrLine2: TextView = itemView.findViewById(R.id.tv_addr_line_2)
        private val defaultAddr: TextView = itemView.findViewById(R.id.tv_default_lbl)
        private val pickupAddr: TextView = itemView.findViewById(R.id.tv_pickup_lbl)
        private val editAddr: ImageView = itemView.findViewById(R.id.iv_edit_addr_btn)

        // Function to set values to the specified views from address item
        internal fun setAddressData(address: Address) {
            // Store the address object values in the respective views
            addrFullName.text = address.fullName
            phoneNum.text = address.phoneNum
            addrLine1.text = address.detailAdd
            addrLine2.text = context.getString(
                R.string.addr_line_2, address.barangay, address.city,
                address.province, address.postal.toString()
            )

            // Set the visibility of address labels depending on the assigned label
            defaultAddr.visibility = if (address.default) View.VISIBLE else View.GONE
            pickupAddr.visibility = if (address.pickup) View.VISIBLE else View.GONE

            // Actions when the Edit icon is clicked
            editAddr.setOnClickListener {
                val bundle = Bundle()  // Bundle object to store parcelable

                // Stores the parcelable class in the Bundle
                bundle.putParcelable(Constants.USER_ADDRESS, address)
                // Sets the arguments for AddressInfoFragment
                AddressInfoFragment().arguments = bundle

                // Sends user to the Address Editor, with address data values
                fragment.findNavController().navigate(
                    R.id.addresses_to_address_info, bundle
                )
            }
        }  // end of setAddressData method

    }  // end of AddressViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.address_item, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: AddressViewHolder, position: Int, address: Address
    ) {
        // Sets the values of address data to the current view
        holder.setAddressData(address)
    }  // end of onBindViewHolder method

}  // end of AddressAdapter class