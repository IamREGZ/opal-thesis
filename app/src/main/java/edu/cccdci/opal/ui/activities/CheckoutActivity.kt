package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CheckoutAdapter
import edu.cccdci.opal.databinding.ActivityCheckoutBinding
import edu.cccdci.opal.dataclasses.*
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class CheckoutActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var checkoutAdapter: CheckoutAdapter
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private var mSelectedAddress: Address? = null
    private var mSelectedPayment: String = ""
    private var mUserInfo: User? = null
    private var mMarket: Market? = null
    private var mCartDetails: List<Product?> = listOf()
    private var mSubtotal: Double = 0.0
    private var mDelivery: Double = 0.0
    private var mOrderActionPos: Int = 0
    private val mOrderHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)

        // Creates the Shared Preferences
        mSharedPrefs = getSharedPreferences(
            Constants.OPAL_PREFERENCES, Context.MODE_PRIVATE
        )
        // Create the editor for Shared Preferences
        mSPEditor = mSharedPrefs.edit()

        /* Make the Shared Preference of Selected Payment Method blank
         * whenever this activity is opened for the first time.
         */
        mSPEditor.putString(Constants.SELECTED_PAYMENT_METHOD, "").apply()

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
            // Get data from the parcelable class
            mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)
        }

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.MARKET_INFO)) {
            // Get data from the parcelable class
            mMarket = intent.getParcelableExtra(Constants.MARKET_INFO)
        }

        // Check if there's an existing parcelable array extra info
        if (intent.hasExtra(Constants.CART_PRODUCT_DETAILS)) {
            // Get data from the parcelable array
            mCartDetails = intent.getParcelableArrayExtra(
                Constants.CART_PRODUCT_DETAILS
            )!!.filterIsInstance<Product?>().toList()
        }

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbCheckoutActivity, false)

            getDefaultAddress()  // Find the user's default address in Firestore
            // Set the drop down values for order unavailable actions spinner
            setOrderUnavailableActions()
            setItemsReceiptData()  // Change the text labels of items receipt

            // Check if mUserInfo and its cart property are not null to prevent NPE
            if (mUserInfo != null && mUserInfo!!.cart != null)
                setupCheckoutAdapter()  // Setup the Checkout RecyclerView Adapter

            // Click event for Select Address ImageView
            ivSelectAddress.setOnClickListener(this@CheckoutActivity)
            // Click event for Payment Method Layout
            llPaymentMethod.setOnClickListener(this@CheckoutActivity)
            // Click event for Place Order Button
            btnPlaceOrder.setOnClickListener(this@CheckoutActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // Operations to do when this activity is visible again
    override fun onRestart() {
        super.onRestart()

        // Get selected address information from Shared Preferences
        val selectedAddress: String = mSharedPrefs.getString(
            Constants.SELECTED_ADDRESS, ""
        )!!
        // Get selected payment information from Shared Preferences
        mSelectedPayment = mSharedPrefs.getString(
            Constants.SELECTED_PAYMENT_METHOD, ""
        )!!

        // If the selected address is not empty, change the selected address object
        if (selectedAddress.isNotEmpty()) {
            // Store the new address value
            mSelectedAddress = Gson().fromJson(
                selectedAddress, Address::class.java
            )

            setCheckoutAddress()  // Change the address text labels
        } else {
            /* If it is empty, make the no selected address label visible
             * and the layout of selected address gone.
             */
            with(binding) {
                if (llSelectedAddress.isVisible) {
                    tvNoSelectedAddress.visibility = View.VISIBLE
                    llSelectedAddress.visibility = View.GONE
                }
            }  // end of with(binding)
        }  // end of if-else

        // If the selected payment is not empty, change the payment method text value
        if (mSelectedPayment.isNotEmpty())
            binding.tvChkoutPaymentMethod.text = mSelectedPayment
    }  // end of onRestart method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Sends user to address selection
                R.id.iv_select_address -> {
                    // Create an intent to launch AddressesActivity
                    val intent = Intent(
                        this@CheckoutActivity,
                        AddressesActivity::class.java
                    )
                    // To enable address selection functionality in the target activity
                    intent.putExtra(Constants.SELECTABLE_ENABLED, true)

                    startActivity(intent)  // Opens the activity
                }

                // Sends user to payment method selection
                R.id.ll_payment_method -> {
                    startActivity(
                        Intent(
                            this@CheckoutActivity,
                            PaymentActivity::class.java
                        )
                    )
                }

                // Confirms user's orders and send to home page
                R.id.btn_place_order -> {
                    placeOrder()
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Function to find user's address labeled as default in Firestore
    private fun getDefaultAddress() {
        // Display the loading message
        showProgressDialog(
            this@CheckoutActivity, this@CheckoutActivity,
            resources.getString(R.string.msg_please_wait)
        )

        // Call the Firestore function to search for an address labeled as default
        FirestoreClass().selectDefaultAddress(this@CheckoutActivity)
    }  // end of getDefaultAddress method

    /* Function to store the address retrieved from Firestore (default) or
     * the selected address from the selection activity.
     */
    fun storeSelectedAddress(address: Address?) {
        hideProgressDialog()  // Hide the loading message

        // Store the address in the variable
        mSelectedAddress = address

        // Change the output of address data if the address is not null
        if (mSelectedAddress != null) {
            /* Set the Shared Preference of selected address as the retrieved
             * default address.
             */
            mSPEditor.putString(
                Constants.SELECTED_ADDRESS, Gson().toJson(mSelectedAddress)
            ).apply()

            setCheckoutAddress()  // Change the delivery address output information
        } else {
            // If no address information found

            // Make the Shared Preference of selected address blank
            mSPEditor.putString(Constants.SELECTED_ADDRESS, "").apply()
        }
    }  // end of storeSelectedAddress method

    // Function to change the output of delivery address information
    private fun setCheckoutAddress() {
        with(binding) {
            // Change all the respective views with address information
            tvChkoutAddrContact.text = getString(
                R.string.address_contact_info, mSelectedAddress!!.fullName,
                mSelectedAddress!!.phoneNum
            )
            tvChkoutAddrLine1.text = mSelectedAddress!!.detailAdd
            tvChkoutAddrLine2.text = getString(
                R.string.addr_line_2, mSelectedAddress!!.barangay,
                mSelectedAddress!!.city, mSelectedAddress!!.province,
                mSelectedAddress!!.postal
            )

            /* Make the select address layout visible and make
             * the no selected address label gone.
             */
            llSelectedAddress.visibility = View.VISIBLE
            tvNoSelectedAddress.visibility = View.GONE
        }  // end of with(binding)
    }  // end of setCheckoutAddress method

    // Function to setup the RecyclerView adapter for checkout
    private fun setupCheckoutAdapter() {
        with(binding) {
            // Sets the layout type of the RecyclerView
            rvItemsChkout.layoutManager = object : LinearLayoutManager(
                this@CheckoutActivity
            ) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false
            }
            // Create an object of Checkout Adapter
            checkoutAdapter = CheckoutAdapter(
                this@CheckoutActivity, mUserInfo!!.cart!!.cartItems,
                mCartDetails
            )
            // Sets the adapter of Checkout RecyclerView
            rvItemsChkout.adapter = checkoutAdapter
        }
    }  // end of setupCheckoutAdapter method

    // Function to set drop down data of order unavailable actions
    private fun setOrderUnavailableActions() {
        // Create an array of order unavailable actions
        val orderActions = resources.getStringArray(R.array.order_unavailable_actions)

        // Prepare the drop down values for order unavailable actions
        val ordActionsAdapter = ArrayAdapter(
            this@CheckoutActivity, R.layout.spinner_item, orderActions
        )
        binding.actvChkoutOrderAction.setAdapter(ordActionsAdapter)

        // Actions when one of the order unavailable action items was selected
        binding.actvChkoutOrderAction.setOnItemClickListener { _, _, position, _ ->
            // Store the current position of selected order unavailable action item
            mOrderActionPos = position
        }
    }  // end of setOrderUnavailableActions

    // Function to store the values of items receipt and market information
    private fun setItemsReceiptData() {
        with(binding) {
            // Check if mUserInfo and its cart property are not null
            if (mUserInfo != null && mUserInfo!!.cart != null) {
                // Get the subtotal of cart items
                mSubtotal = mUserInfo!!.cart!!.cartItems.sumOf { it.prodPrice }

                // Change the subtotal text value
                tvChkoutSubtotal.text = getString(
                    R.string.item_price, mSubtotal
                )

                // Check if mMarket is not null
                if (mMarket != null) {
                    // Get the delivery fee
                    mDelivery = mMarket!!.deliveryFee

                    // Change the market name text and delivery fee
                    tvChkoutMarket.text = mMarket!!.name
                    tvChkoutDelivery.text = getString(
                        R.string.item_price, mDelivery
                    )

                    // Change the total texts by adding mSubtotal and mDelivery
                    tvChkoutTotal.text = getString(
                        R.string.item_price, mSubtotal + mDelivery
                    )
                    tvChkoutTotalPayment.text = getString(
                        R.string.item_price, mSubtotal + mDelivery
                    )
                }  // end of if

            }  // end of if
        }  // end of with(binding)

    }  // end of setItemsReceiptData method

    // Function to validate checkout information
    private fun validateCheckout(): Boolean {
        return when {
            // If no address is selected
            mSelectedAddress == null -> {
                // Display an error message
                showSnackBar(
                    this@CheckoutActivity,
                    resources.getString(R.string.err_no_selected_address),
                    true
                )
                false  // return false
            }

            // If no order action is selected
            TextUtils.isEmpty(binding.actvChkoutOrderAction.text
                .toString().trim { it <= ' ' }) -> {
                // Display an error message
                showSnackBar(
                    this@CheckoutActivity,
                    resources.getString(R.string.err_no_selected_order_action),
                    true
                )
                false  // return false
            }

            // If no payment method is selected
            mSelectedPayment.isEmpty() -> {
                // Display an error message
                showSnackBar(
                    this@CheckoutActivity,
                    resources.getString(R.string.err_no_selected_payment),
                    true
                )
                false  // return false
            }

            else -> true  // If all of the requirements are valid
        }  // end of when
    }  // end of validateCheckout method

    // Function to proceed to place user's order(s)
    private fun placeOrder() {
        /* Validate first the required checkout fields (delivery address,
         * order action & payment method).
         */
        if (validateCheckout()) {
            // Display the loading message
            showProgressDialog(
                this@CheckoutActivity, this@CheckoutActivity,
                resources.getString(R.string.msg_please_wait)
            )

            // Get all the checkout data and store them in the HashMap
            storeOrderValues()

            // Store the retrieved data to Firestore
            FirestoreClass().addCustomerOrder(
                this@CheckoutActivity, mOrderHashMap
            )
        }  // end of if

    }  // end of placeOrder method

    // Function to store order values in the Order HashMap
    private fun storeOrderValues() {
        // Store the Order ID
        mOrderHashMap[Constants.ID] = Constants.ORDER_ID_TEMP +
                System.currentTimeMillis().toString()

        // Store the Order Dates, with a defined Order Date value
        mOrderHashMap[Constants.DATES] = hashMapOf<String, Any?>(
            Constants.ORDER_DATE to FieldValue.serverTimestamp(),
            Constants.PAYMENT_DATE to null,
            Constants.DELIVER_DATE to null,
            Constants.RETURN_DATE to null
        )

        // Store the Order Status Code, with 0 (Pending) as initial code
        mOrderHashMap[Constants.STATUS] = Constants.ORDER_PENDING_CODE

        // Store the Payment Method
        mOrderHashMap[Constants.ORDER_PAYMENT] = mSelectedPayment

        // Prevents NullPointerException
        if (mSelectedAddress != null) {
            // Store the user's address information
            mOrderHashMap[Constants.ADDRESS] = OrderAddress(
                mSelectedAddress!!.fullName,
                mSelectedAddress!!.phoneNum,
                mSelectedAddress!!.province,
                mSelectedAddress!!.city,
                mSelectedAddress!!.barangay,
                mSelectedAddress!!.postal,
                mSelectedAddress!!.detailAdd
            )
        }  // end of if

        // Prevents NullPointerException
        if (mMarket != null) {
            // Store the Market ID
            mOrderHashMap[Constants.MARKET_ID] = mMarket!!.id
            // Store the Market name
            mOrderHashMap[Constants.MARKET_NAME] = mMarket!!.name
            // Store the Vendor ID
            mOrderHashMap[Constants.VENDOR_ID] = mMarket!!.vendorID
        }  // end of if

        // Store the Subtotal of all order item prices
        mOrderHashMap[Constants.SUB_TOTAL] = mSubtotal

        // Store the Market's delivery fee
        mOrderHashMap[Constants.DELIVERY_FEE_PRICE] = mDelivery

        // Store the Total Price (sum of subtotal and delivery fee)
        mOrderHashMap[Constants.TOTAL_PRICE] = mSubtotal + mDelivery

        // Store the selected value in the Order Unavailable Action Spinner
        mOrderHashMap[Constants.ORDER_ACTION] = mOrderActionPos

        /* Store the inputted value in the Special Instructions Text Area
         * (blank input is accepted since it is optional).
         */
        mOrderHashMap[Constants.SPECIAL_INSTRUCTIONS] = binding
            .etChkoutSpecialInstructions.text.toString().trim { it <= ' ' }

        // Prevents NullPointerException
        if (mUserInfo != null) {
            // Store the user's ID as Customer ID
            mOrderHashMap[Constants.CUSTOMER_ID] = mUserInfo!!.id
            // Store the user's username
            mOrderHashMap[Constants.CUSTOMER_USERNAME] = mUserInfo!!.userName

            // A variable to store all Order Items' information
            val orderItems: MutableList<OrderItem> = mutableListOf()

            // Loop through mCartDetails using index
            for (i in mCartDetails.indices) {
                // Prevents NullPointerException
                if (mUserInfo!!.cart != null && mCartDetails[i] != null) {
                    // For each item, add the required information in the list
                    orderItems.add(
                        OrderItem(
                            mCartDetails[i]!!.id,
                            mCartDetails[i]!!.name,
                            mCartDetails[i]!!.image,
                            mCartDetails[i]!!.price,
                            mCartDetails[i]!!.unit,
                            mCartDetails[i]!!.weight,
                            mUserInfo!!.cart!!.cartItems[i].prodQTY,
                            mUserInfo!!.cart!!.cartItems[i].prodPrice
                        )
                    )
                }  // end of if

            }  // end of for

            // Finally, store the user's Order Items
            mOrderHashMap[Constants.ORDER_ITEMS] = orderItems
        }  // end of if

    }  // end of storeOrderValues method

    /* Function to prompt that the user's checkout data is stored in
     * the database, and then proceed to empty the user's cart
     */
    fun orderPlacedPrompt() {
        // Update the cart by emptying the items
        FirestoreClass().updateCart(
            this@CheckoutActivity, emptyList(), toClear = true
        )
    }  // end of orderPlacedPrompt method

    // Function to prompt that the cart data was cleared and then sends user to home page
    fun cartClearedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Create an Intent to launch MainActivity
        val intent = Intent(
            this@CheckoutActivity, MainActivity::class.java
        )
        // To ensure that no more activity layers are active after the user places order
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Displays a Toast message
        toastMessage(
            this@CheckoutActivity,
            resources.getString(R.string.msg_order_placed_success),
            true
        )

        startActivity(intent)  // Opens the home page
        finish()  // Closes the activity
    }  // end of cartClearedPrompt method

}  // end of CheckoutActivity class