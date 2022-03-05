package edu.cccdci.opal.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.utils.Constants

class PaymentAdapter(
    private val activity: Activity,
    private val paymentsList: List<HashMap<String, String>>
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class PaymentViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from payment item layout
        private val paymentName: TextView = itemView
            .findViewById(R.id.tv_payment_name)
        private val paymentSelect: ImageView = itemView
            .findViewById(R.id.iv_payment_selection)
        private val paymentItem: LinearLayout = itemView
            .findViewById(R.id.ll_payment_method_item)

        // Function to set values to the specified views from payment item
        internal fun setPaymentData(payment: HashMap<String, String>) {
            // Set the payment name
            paymentName.text = payment[Constants.PAYMENT_METHOD]

            /* Change the radio button image to checked if the selection
             * status is 1 (true).
             */
            if (payment[Constants.IS_SELECTED] == "1")
                paymentSelect.setImageResource(R.drawable.ic_radio_button_checked)

            // Actions when the Payment Method Item Layout is clicked
            paymentItem.setOnClickListener {
                // Creates the Shared Preferences
                val sharedPrefs = activity.getSharedPreferences(
                    Constants.OPAL_PREFERENCES,
                    Context.MODE_PRIVATE
                )

                // Create the editor for Shared Preferences
                val spEditor: SharedPreferences.Editor = sharedPrefs.edit()

                // Shared Preference for Selected Payment Method
                spEditor.putString(
                    Constants.SELECTED_PAYMENT_METHOD,
                    payment[Constants.PAYMENT_METHOD]
                ).apply()

                activity.finish()  // Closes the activity
            }
        }  // end of setPaymentData method

    }  // end of PaymentViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PaymentViewHolder {
        return PaymentViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.payment_item, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        // Get an object from the current position of payment item list
        val paymentItem = paymentsList[position]

        // Sets the values of payment item data to the current view
        holder.setPaymentData(paymentItem)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = paymentsList.size

}  // end of PaymentAdapter class
