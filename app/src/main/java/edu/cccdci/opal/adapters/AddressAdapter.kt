package edu.cccdci.opal.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Address
import edu.cccdci.opal.ui.activities.AddressEditActivity
import edu.cccdci.opal.utils.Constants

class AddressAdapter(
    private val activity: Activity,
    private val selectable: Boolean,
    options: FirestoreRecyclerOptions<Address>
): FirestoreRecyclerAdapter<Address, AddressAdapter.AddressViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class AddressViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from address item layout
        private val addrFullName: TextView = itemView
            .findViewById(R.id.tv_addr_full_name)
        private val phoneNum: TextView = itemView
            .findViewById(R.id.tv_addr_phone)
        private val addrLine1: TextView = itemView
            .findViewById(R.id.tv_addr_line_1)
        private val addrLine2: TextView = itemView
            .findViewById(R.id.tv_addr_line_2)
        private val defaultAddr: TextView = itemView
            .findViewById(R.id.tv_default_lbl)
        private val editAddr: ImageView = itemView
            .findViewById(R.id.iv_edit_addr_btn)
        private val userAddrPanel: LinearLayout = itemView
            .findViewById(R.id.ll_address_panel)

        // Function to set values to the specified views from address item
        internal fun setAddressData(address: Address) {
            // Store the address object values in the respective views
            addrFullName.text = address.fullName
            phoneNum.text = address.phoneNum
            addrLine1.text = address.detailAdd
            addrLine2.text = activity.getString(
                R.string.addr_line_2, address.barangay, address.city,
                address.province, address.postal
            )

            // Set the visibility of address labels depending on the assigned label
            defaultAddr.visibility = if (address.default) View.VISIBLE else View.GONE

            // Actions when the Edit icon is clicked
            editAddr.setOnClickListener {
                // Create an Intent to launch AddressEditActivity
                val intent = Intent(activity, AddressEditActivity::class.java)

                // Stores the parcelable class
                intent.putExtra(Constants.USER_ADDRESS, address)

                // Opens the address editor
                activity.startActivity(intent)
            }

            // Enable address selection if the preceding activity is Checkout
            if (selectable) {
                // Actions when the Address Item Layout is clicked
                userAddrPanel.setOnClickListener {
                    // Creates the Shared Preferences
                    val sharedPrefs = activity.getSharedPreferences(
                        Constants.OPAL_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                    // Create the editor for Shared Preferences
                    val spEditor: SharedPreferences.Editor = sharedPrefs.edit()

                    // Shared Preference for Selected Address
                    spEditor.putString(
                        Constants.SELECTED_ADDRESS, Gson().toJson(address)
                    ).apply()

                    activity.finish()  // Closes the activity
                }
            }  // end of if
        }  // end of setAddressData method

    }  // end of AddressViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AddressViewHolder {
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