package edu.cccdci.opal.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.PaymentAdapter
import edu.cccdci.opal.databinding.ActivityPaymentBinding
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class PaymentActivity : UtilityClass() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentAdapter: PaymentAdapter
    private val mPaymentData: MutableList<HashMap<String, String>> = mutableListOf()
    private var mSelectedPayment: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbPaymentActivity, false)

            setupPaymentList()  // Start by creating the list of payments
        }  // end of with(binding)

    }  // end of onCreate method

    // Function to setup the list of payment methods
    private fun setupPaymentList() {
        // A list containing all payment methods
        val payments: List<String> = resources.getStringArray(
            R.array.payment_methods
        ).toList()

        /* For each payments in the list, create a hash map to store its
         * name and selection status (0 for false, 1 for true).
         */
        payments.forEach {
            mPaymentData.add(
                hashMapOf(
                    Constants.PAYMENT_METHOD to it,
                    Constants.IS_SELECTED to "0"
                )
            )
        }

        // Creates the Shared Preferences
        val sharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )

        // Get selected payment information from Shared Preferences
        mSelectedPayment = sharedPrefs.getString(
            Constants.SELECTED_PAYMENT_METHOD, ""
        )!!

        // If the selected payment is not currently selected
        if (mSelectedPayment.isNotEmpty()) {
            /* Search through the list of payments and if the selected
             * payment is found in the list, change the selection status
             * into 1 (true) and the stop the loop.
             */
            for (pym in mPaymentData) {
                if (pym[Constants.PAYMENT_METHOD] == mSelectedPayment) {
                    pym[Constants.IS_SELECTED] = "1"
                    break
                }
            }
        }  // end of if

        // Proceed to setup the payment RecyclerView adapter
        setupPaymentAdapter()
    }  // end of setupPaymentList method

    // Function to setup the RecyclerView adapter for payments
    private fun setupPaymentAdapter() {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvPayments.layoutManager = LinearLayoutManager(
                this@PaymentActivity, LinearLayoutManager.VERTICAL,
                false
            )
            // Create an object of Payment Adapter
            paymentAdapter = PaymentAdapter(this@PaymentActivity, mPaymentData)
            // Sets the adapter of Payment RecyclerView
            rvPayments.adapter = paymentAdapter
        }  // end of with(binding)
    }  // end of setupPaymentAdapter method

}  // end of PaymentActivity class