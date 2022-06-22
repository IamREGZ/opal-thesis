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
import edu.cccdci.opal.dataclasses.CurrentLocation
import edu.cccdci.opal.dataclasses.UserAddress
import edu.cccdci.opal.ui.activities.AddressEditActivity
import edu.cccdci.opal.utils.Constants

class AddressAdapter(
    private val activity: Activity,
    private val selectable: Boolean,
    private val mode: Int,
    options: FirestoreRecyclerOptions<UserAddress>
) : FirestoreRecyclerAdapter<UserAddress, AddressAdapter.AddressViewHolder>(options) {

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
        internal fun setAddressData(uAddress: UserAddress) {
            // Store the address object values in the respective views
            addrFullName.text = uAddress.fullName
            phoneNum.text = uAddress.phoneNum
            addrLine1.text = uAddress.detailAdd
            addrLine2.text = activity.getString(
                R.string.addr_line_2, uAddress.barangay, uAddress.city,
                uAddress.province, uAddress.postal
            )

            /* Set the visibility of default address label depending the
             * default address status
             */
            defaultAddr.visibility = if (uAddress.default) View.VISIBLE else View.GONE

            // Actions when the Edit icon is clicked
            editAddr.setOnClickListener {
                // Opens the address editor
                activity.startActivity(openEditorWithAddress(uAddress))
            }

            // Enable address selection if the preceding activity is Checkout or Home
            if (selectable) {
                // Actions when the Address Item Layout is clicked
                userAddrPanel.setOnClickListener {
                    // Selects the address and sends back the user to previous page
                    selectAddress(uAddress)
                }
            }  // end of if

        }  // end of setAddressData method

        // Function to return an Intent with Address class parcelable
        private fun openEditorWithAddress(uAddress: UserAddress): Intent {
            // Create an Intent to launch AddressEditActivity
            return Intent(activity, AddressEditActivity::class.java).apply {
                // Stores the parcelable class
                putExtra(Constants.USER_ADDRESS, uAddress)
            }
        }  // end of openEditorWithAddress class

        // Function to select this address and do something depending on the context
        private fun selectAddress(uAddress: UserAddress) {
            // Creates the Shared Preferences
            val sharedPrefs = activity.getSharedPreferences(
                Constants.OPAL_PREFERENCES,
                Context.MODE_PRIVATE
            )

            // Create the editor for Shared Preferences
            val spEditor: SharedPreferences.Editor = sharedPrefs.edit()

            with(uAddress) {
                spEditor.apply {
                    when (mode) {
                        // Address selection for Checkout
                        Constants.SELECT_CHECKOUT_ADDRESS -> putString(
                            Constants.SELECTED_ADDRESS, Gson().toJson(this@with)
                        )

                        // Address selection for Current Location
                        Constants.SELECT_CURRENT_LOCATION -> putString(
                            Constants.CURRENT_LOCATION, Gson().toJson(
                                CurrentLocation(
                                    Constants.FROM_USER_ADDRESS_CODE,
                                    location?.latitude ?: 0.0,
                                    location?.longitude ?: 0.0,
                                    "$detailAdd, $barangay, $city, $province $postal"
                                )
                            )
                        )
                    }  // end of when
                }.apply()
            }  // end of with(address)

            activity.finish()  // Closes the activity

        }  // end of selectAddress method

    }  // end of AddressViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AddressViewHolder {
        return AddressViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_address, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: AddressViewHolder, position: Int, uAddress: UserAddress
    ) {
        // Sets the values of address data to the current view
        holder.setAddressData(uAddress)
    }  // end of onBindViewHolder method

}  // end of AddressAdapter class
