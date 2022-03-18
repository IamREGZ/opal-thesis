package edu.cccdci.opal.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CheckoutAdapter
import edu.cccdci.opal.databinding.ActivityCheckoutBinding
import edu.cccdci.opal.dataclasses.*
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GeoDistance
import edu.cccdci.opal.utils.UtilityClass

class CheckoutActivity
    : UtilityClass(), View.OnClickListener, OnMapReadyCallback,
    GeoDistance.GeoDistanceResult, TextWatcher {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var checkoutAdapter: CheckoutAdapter
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSPEditor: SharedPreferences.Editor
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private var mSelectedAddress: Address? = null
    private var mIsInDeliveryCoverage: Boolean = false
    private var mSelectedPayment: String? = ""
    private var mUserInfo: User? = null
    private var mMarket: Market? = null
    private var mSelectedCartItems: List<CartItem> = emptyList()
    private var mCartDetails: List<Product> = emptyList()
    private var mDelivery: Double = 0.0
    private var mOrderActionPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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

        // Prepare the SupportMapFragment
        mSupportMap = supportFragmentManager
            .findFragmentById(R.id.mpfr_checkout_address) as SupportMapFragment

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbCheckoutActivity, false)

            // Check if there's an existing parcelable array extra info
            if (intent.hasExtra(Constants.CART_PRODUCT_DETAILS)) {
                // Get data from the parcelable array
                mCartDetails = intent.getParcelableArrayExtra(
                    Constants.CART_PRODUCT_DETAILS
                )!!.filterIsInstance<Product>()
            }

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                // Get data from the parcelable class
                mUserInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)

                // Check if mUserInfo and its cart property are not null to prevent NPE
                if (mUserInfo != null && mUserInfo!!.cart != null) {
                    mSelectedCartItems = mUserInfo!!.cart!!.cartItems
                        .filter { it.selected }

                    setupCheckoutAdapter()  // Setup the Checkout RecyclerView Adapter
                }
            }

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.MARKET_INFO)) {
                // Get data from the parcelable class
                mMarket = intent.getParcelableExtra(Constants.MARKET_INFO)

                // Change the market name
                tvChkoutMarket.text = mMarket?.name ?: "undefined"
            }

            // Find the user's default address in Firestore
            getDefaultAddress()

            // Set the drop down values for order unavailable actions spinner
            setOrderUnavailableActions()

            // Click event for Select Address ImageView
            ivSelectAddress.setOnClickListener(this@CheckoutActivity)
            // Click event for Payment Method Layout
            llPaymentMethod.setOnClickListener(this@CheckoutActivity)
            // Click event for Place Order Button
            btnPlaceOrder.setOnClickListener(this@CheckoutActivity)
            // Text changed event for Special Instructions EditText
            etChkoutSpecialInstructions.addTextChangedListener(this@CheckoutActivity)
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
        )

        with(binding) {
            // If the selected address is not empty, change the selected address object
            if (selectedAddress.isNotEmpty()) {
                // Store the new address value
                mSelectedAddress = Gson().fromJson(selectedAddress, Address::class.java)

                setCheckoutAddress()  // Change the address text labels
            } else {
                /* If it is empty, make the no selected address label visible
                 * and the layout of selected address gone.
                 */
                if (llSelectedAddress.isVisible) {
                    tvNoSelectedAddress.visibility = View.VISIBLE
                    llSelectedAddress.visibility = View.GONE
                }

                // Change the delivery message attributes
                toggleDeliveryMessagePanel(false)
                // Change the text labels of items receipt, with 0.0 as distance
                setItemsReceiptData(0.0)
            }  // end of if-else

            /* Change the payment method text value. "Select Payment Method"
             * if mSelectedPayment is null or empty.
             */
            tvChkoutPaymentMethod.text = mSelectedPayment?.ifEmpty {
                getString(R.string.select_payment)
            } ?: getString(R.string.select_payment)
        }

    }  // end of onRestart method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Sends user to address selection
                R.id.iv_select_address -> {
                    // Create an intent to launch AddressesActivity
                    Intent(
                        this@CheckoutActivity, AddressesActivity::class.java
                    ).apply {
                        // To enable address selection functionality in the target activity
                        putExtra(Constants.SELECTABLE_ENABLED, true)
                        putExtra(Constants.SELECTION_MODE, 0)

                        startActivity(this)  // Opens the activity
                    }  // end of apply
                }

                // Sends user to payment method selection
                R.id.ll_payment_method -> startActivity(
                    Intent(
                        this@CheckoutActivity,
                        PaymentActivity::class.java
                    )
                )

                // Confirms user's orders and send to home page
                R.id.btn_place_order -> placeOrder()
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Overriding function to set the Map UI of current delivery address
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object

        mGoogleMap.apply {
            clear()  // Clear all the markers set in the map

            // Get the delivery address coordinates
            val delivery = Constants.getLocation(mSelectedAddress)
            // Create an object of delivery address' latitude and longitude
            val deliveryLoc = LatLng(delivery[0], delivery[1])

            // Set the delivery marker attributes
            val deliveryMarker = MarkerOptions().apply {
                // Set the position to the latitude and longitude of selected address
                position(LatLng(delivery[0], delivery[1]))
                // Customize the icon image
                icon(
                    BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_map_marker_primary)
                )
            }

            addMarker(deliveryMarker)  // Make the marker visible to the Map UI
            // Focus the Map UI to the position of marker
            moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLoc, 18f))

            // Disable all touch interactions and toolbar of the Map UI
            uiSettings.setAllGesturesEnabled(false)
            uiSettings.isMapToolbarEnabled = false

            setOnMapClickListener {
                /* Make sure the address is within the market's delivery coverage
                 * to view the route.
                 */
                if (mIsInDeliveryCoverage) {
                    // Get the market address coordinates
                    val market = Constants.getLocation(mMarket)
                    // Create an object of market's latitude and longitude
                    val marketLoc = LatLng(market[0], market[1])

                    // Create an Intent to launch MapActivity
                    Intent(this@CheckoutActivity, MapActivity::class.java)
                        .apply {
                            // Add an array of locations (market and delivery) to the intent
                            putExtra(
                                Constants.LOCATION_MARKERS_INFO,
                                arrayOf(marketLoc, deliveryLoc)
                            )
                            // Add the market name to the intent
                            putExtra(Constants.MARKET_NAME_DATA, mMarket!!.name)

                            startActivity(this)  // Opens the activity
                        }  // end of apply
                } else {
                    /* Display a message that the selected address is not within the
                     * market's delivery coverage.
                     */
                    showSnackBar(
                        this@CheckoutActivity,
                        getString(R.string.err_route_not_in_coverage),
                        true
                    )
                }  // end of if-else
            }  // end of setOnMapClickListener

        }  // end of apply

    }  // end of onMapReady method

    // Overriding function to get the result of distance and duration calculations
    override fun setDistanceResult(res: String) {
        // Extract the values, separated by commas
        val distRes: List<String> = res.split(',')
        val distance = distRes[0].toDouble() / 1000

        with(binding) {
            // Change the delivery duration in the delivery message panel
            tvChkoutDeliveryDuration.text = getString(
                R.string.chkout_delivery_duration, distRes[1].toInt() / 60
            )

            // Get the delivery fee
            mDelivery = if (distance < 1 || distance >= 8) {
                // 0-0.999 km, free delivery. > 8 km, not in delivery coverage
                0.0
            } else {
                // First km, 20. Additional 5 for succeeding km up to 9.999 km (max 60)
                mMarket?.let { it.deliveryFee + (5 * (distance.toInt() - 1)) } ?: 0.0
            }

            /* Change the delivery message attributes. true if distance is greater
             * than 0 and less than or equal to 8.
             */
            toggleDeliveryMessagePanel(distance > 0 && distance <= 8)
            // Change the text labels of items receipt with distance value
            setItemsReceiptData(distance)
        }  // end of with(binding)

    }  // end of setDistanceResult method

    // For Special Instructions EditText only
    // Events during changes in the text field
    override fun onTextChanged(
        text: CharSequence?, start: Int, before: Int, count: Int
    ) {
        /* In every change in Special Instructions EditText, observe if the
         * number of characters is less than or equal to 100. And then check
         * if the criteria for enabling Place Order Button are met.
         */
        togglePlaceOrderButton()
    }  // end of onTextChanged method

    // Events before the text field is changed
    // NOTE: Empty function, do not add codes
    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

    // Events after the text field was changed
    // NOTE: Empty function, do not add codes
    override fun afterTextChanged(field: Editable?) {}

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
                this@CheckoutActivity, mSelectedCartItems, mCartDetails
            )

            // Sets the adapter of Checkout RecyclerView
            rvItemsChkout.adapter = checkoutAdapter
        }  // end of with(binding)
    }  // end of setupCheckoutAdapter method

    // Function to find user's address labeled as default in Firestore
    private fun getDefaultAddress() {
        // Display the loading message
        showProgressDialog(
            this@CheckoutActivity, this@CheckoutActivity,
            getString(R.string.msg_please_wait)
        )

        // Call the Firestore function to search for an address labeled as default
        FirestoreClass().findDefaultAddress(this@CheckoutActivity)
    }  // end of getDefaultAddress method

    /* Function to store the address retrieved from Firestore (default) or
     * the selected address from the selection activity.
     */
    internal fun storeSelectedAddress(address: Address?) {
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
        }
        // If no address information was found
        else {
            // Make the Shared Preference of selected address blank
            mSPEditor.putString(Constants.SELECTED_ADDRESS, "").apply()

            if (binding.llSelectedAddress.isVisible) {
                binding.llSelectedAddress.visibility = View.GONE
                binding.tvNoSelectedAddress.visibility = View.VISIBLE
            }

            // Change the delivery message attributes
            toggleDeliveryMessagePanel(false)
            // Change the text labels of items receipt, with 0.0 as distance
            setItemsReceiptData(0.0)
        }  // end of if-else

    }  // end of storeSelectedAddress method

    // Function to change the output of delivery address information
    private fun setCheckoutAddress() {
        with(binding) {
            mSelectedAddress?.let { addr ->
                // Change all the respective views with address information
                tvChkoutAddrContact.text = getString(
                    R.string.address_contact_info, addr.fullName, addr.phoneNum
                )
                tvChkoutAddrLine1.text = addr.detailAdd
                tvChkoutAddrLine2.text = getString(
                    R.string.addr_line_2, addr.barangay, addr.city,
                    addr.province, addr.postal
                )

                // Load the map fragment
                mSupportMap.getMapAsync(this@CheckoutActivity)

                /* Make the select address layout visible and make
                 * the no selected address label gone.
                 */
                if (!llSelectedAddress.isVisible) {
                    llSelectedAddress.visibility = View.VISIBLE
                    tvNoSelectedAddress.visibility = View.GONE
                }

                // Prevents NullPointerException
                if (mMarket != null && mMarket!!.location != null) {
                    /* Check if delivery address is within the market's delivery
                     * coverage
                     */
                    FirestoreClass().getNearbyLocations(
                        this@CheckoutActivity,
                        GeoLocation(
                            mMarket!!.location!!.latitude,
                            mMarket!!.location!!.longitude
                        ),
                        addr
                    )
                } else {
                    // Make the delivery message panel not visible
                    cvDeliveryMessagePanel.visibility = View.GONE
                    // Change the text labels of items receipt, with 0.0 as distance
                    setItemsReceiptData(0.0)
                }
            } ?: run {
                /* Make the selected address not visible and no selected
                 * address label visible
                 */
                if (llSelectedAddress.isVisible) {
                    llSelectedAddress.visibility = View.GONE
                    tvNoSelectedAddress.visibility = View.VISIBLE
                }

                // Change the delivery message attributes
                toggleDeliveryMessagePanel(false)
                // Change the text labels of items receipt, with 0.0 as distance
                setItemsReceiptData(0.0)
            }  // end of let & run
        }  // end of with(binding)

    }  // end of setCheckoutAddress method

    // Function to check if the address is in the delivery coverage of the market
    internal fun checkAddressCoverage(isWithin: Boolean) {
        if (isWithin) {
            // Calculate the distance and delivery duration using coordinates
            GeoDistance(this@CheckoutActivity)
                .calculateDistance(
                    Constants.getLocation(mMarket),
                    Constants.getLocation(mSelectedAddress)
                )
        } else {
            // Change the delivery message attributes
            toggleDeliveryMessagePanel(false)
            // Change the text labels of items receipt, with 0.0 as distance
            setItemsReceiptData(0.0)
        }  // end of if-else
    }  // end of checkAddressCoverage method

    // Function to set drop down data of order unavailable actions
    private fun setOrderUnavailableActions() {
        // Prepare the drop down values for order unavailable actions
        binding.actvChkoutOrderAction.setAdapter(
            ArrayAdapter(
                this@CheckoutActivity, R.layout.spinner_item,
                resources.getStringArray(R.array.order_unavailable_actions)
            )
        )

        // Actions when one of the order unavailable action items was selected
        binding.actvChkoutOrderAction.setOnItemClickListener { _, _, position, _ ->
            // Store the current position of selected order unavailable action item
            mOrderActionPos = position

            /* Upon selection of Order Unavailable Action item, check if all
             * of the criteria for enabling Place Order Button are met.
             */
            togglePlaceOrderButton()
        }
    }  // end of setOrderUnavailableActions

    // Function to store the values of items receipt
    private fun setItemsReceiptData(distance: Double) {
        with(binding) {
            // If not within delivery coverage, delivery fee will be 0.0
            if (distance <= 0 || distance > 8) mDelivery = 0.0

            /* Change the delivery fee label [with the resulting distance if
             * distance is greater than 0 (km) and less than or equal to 8 (km)]
             */
            tvChkoutDeliveryFeeDistance.text = if (distance > 0 && distance <= 8)
                getString(R.string.chkout_delivery_fee_distance, distance)
            else
                getString(R.string.delivery_fee)

            // Change the subtotal and delivery fee text value
            tvChkoutSubtotal.text = getString(
                R.string.item_price, mSelectedCartItems.sumOf { it.prodPrice }
            )
            tvChkoutDelivery.text = getString(R.string.item_price, mDelivery)

            // Change the total texts by adding mSubtotal and mDelivery
            tvChkoutTotal.text = getString(
                R.string.item_price,
                mSelectedCartItems.sumOf { it.prodPrice } + mDelivery
            )
            tvChkoutTotalPayment.text = getString(
                R.string.item_price,
                mSelectedCartItems.sumOf { it.prodPrice } + mDelivery
            )
        }  // end of with(binding)

    }  // end of setItemsReceiptData method

    // Function to toggle delivery message panel attributes
    @SuppressLint("ResourceType")
    private fun toggleDeliveryMessagePanel(deliverable: Boolean) {
        // Set the value of mIsInDeliveryCoverage to the passed value of deliverable
        mIsInDeliveryCoverage = deliverable

        with(binding) {
            // Make the delivery message panel visible if there's an address selected
            cvDeliveryMessagePanel.visibility = if (mSelectedAddress != null)
                View.VISIBLE
            else
                View.GONE

            // Has a selected address and within delivery coverage
            if (mSelectedAddress != null && mIsInDeliveryCoverage) {
                /* Make the delivery info visible and address not in delivery
                 * coverage message not visible
                 */
                llChkoutDeliveryInfo.visibility = View.VISIBLE
                tvAddressNotInCoverage.visibility = View.GONE

                // Set the delivery message panel color to green
                cvDeliveryMessagePanel.setCardBackgroundColor(
                    Color.parseColor(getString(R.color.colorSuccessMessage))
                )
                // Change the delivery message icon to delivery
                ivDeliveryMessageIcon.setImageResource(
                    R.drawable.ic_order_out_for_delivery
                )
            }
            // Has a selected address only
            else if (mSelectedAddress != null) {
                /* Make the address not in delivery coverage message visible
                 * and delivery info not visible
                 */
                llChkoutDeliveryInfo.visibility = View.GONE
                tvAddressNotInCoverage.visibility = View.VISIBLE

                // Set the delivery message panel color to red
                cvDeliveryMessagePanel.setCardBackgroundColor(
                    Color.parseColor(getString(R.color.colorErrorMessage))
                )
                // Change the delivery message icon to round warning icon
                ivDeliveryMessageIcon.setImageResource(
                    R.drawable.ic_warning_round_outline
                )
            }  // end of if-else if
        }  // end of with(binding)

        /* Upon changing the delivery message panel attributes, check if all
         * of the criteria for enabling Place Order Button are met.
         */
        togglePlaceOrderButton()
    }  // end of toggleDeliveryMessagePanel method

    // Function to enable Place Order Button based on the criteria below
    private fun togglePlaceOrderButton() {
        with(binding) {
            /* Criteria for enabling the Place Order Button:
             * 1. There must be one selected delivery address,
             * 2. The delivery address is within the market's delivery coverage,
             * 3. There must be one selected order unavailable action,
             * 4. There must be one selected payment method, and
             * 5. Special instructions (optional) must have at most 100 characters.
             */
            btnPlaceOrder.isEnabled = mSelectedAddress != null &&
                    mIsInDeliveryCoverage &&
                    !TextUtils.isEmpty(actvChkoutOrderAction.text.toString()
                        .trim { it <= ' ' }) &&
                    mSelectedPayment != null && mSelectedPayment!!.isNotEmpty() &&
                    etChkoutSpecialInstructions.text.toString()
                        .trim { it <= ' ' }.length <= 100
        }  // end of with(binding)

    }  // end of togglePlaceOrderButton method

    // Function to proceed to place user's order(s)
    private fun placeOrder() {
        // Display the loading message
        showProgressDialog(
            this@CheckoutActivity, this@CheckoutActivity,
            getString(R.string.msg_please_wait)
        )

        // Store the retrieved data to Firestore
        FirestoreClass().addCustomerOrder(this@CheckoutActivity, storeOrderValues())
    }  // end of placeOrder method

    // Function to store order values in the Order HashMap
    private fun storeOrderValues(): HashMap<String, Any> {
        // Variable to store the resulting checkout data in the HashMap
        val orderHashMap: HashMap<String, Any> = hashMapOf()

        // Store the Order ID
        orderHashMap[Constants.ID] = Constants.ORDER_ID_TEMP +
                System.currentTimeMillis().toString()

        // Store the Order Dates, with a defined Order Date value
        orderHashMap[Constants.DATES] = hashMapOf<String, Any?>(
            Constants.ORDER_DATE to FieldValue.serverTimestamp(),
            Constants.PAYMENT_DATE to null,
            Constants.DELIVER_DATE to null,
            Constants.RETURN_DATE to null
        )

        // Store the Order Status Code, with 0 (Pending) as initial code
        orderHashMap[Constants.STATUS] = Constants.ORDER_PENDING_CODE

        // Store the Payment Method
        orderHashMap[Constants.ORDER_PAYMENT] = mSelectedPayment ?: ""

        orderHashMap[Constants.ADDRESS] = mSelectedAddress?.let {
            // Store the user's address information
            OrderAddress(
                it.fullName, it.phoneNum, it.province, it.city,
                it.barangay, it.postal, it.detailAdd
            )
        } ?: OrderAddress()

        // Store the Market ID
        orderHashMap[Constants.MARKET_ID] = mMarket?.id ?: ""
        // Store the Market name
        orderHashMap[Constants.MARKET_NAME] = mMarket?.name ?: ""
        // Store the Vendor ID
        orderHashMap[Constants.VENDOR_ID] = mMarket?.vendorID ?: ""

        // Store the Subtotal of all order item prices
        orderHashMap[Constants.SUB_TOTAL] = mSelectedCartItems.sumOf { it.prodPrice }

        // Store the Market's delivery fee
        orderHashMap[Constants.DELIVERY_FEE_PRICE] = mDelivery

        // Store the Total Price (sum of subtotal and delivery fee)
        orderHashMap[Constants.TOTAL_PRICE] = mSelectedCartItems
            .sumOf { it.prodPrice } + mDelivery

        // Store the selected value in the Order Unavailable Action Spinner
        orderHashMap[Constants.ORDER_ACTION] = mOrderActionPos

        /* Store the inputted value in the Special Instructions Text Area
         * (blank input is accepted since it is optional).
         */
        orderHashMap[Constants.SPECIAL_INSTRUCTIONS] = binding
            .etChkoutSpecialInstructions.text.toString().trim { it <= ' ' }

        // Store the user's ID as Customer ID
        orderHashMap[Constants.CUSTOMER_ID] = mUserInfo?.id ?: ""
        // Store the user's username
        orderHashMap[Constants.CUSTOMER_USERNAME] = mUserInfo?.userName ?: ""

        // A variable to store all Order Items' information
        val orderItems: MutableList<OrderItem> = mutableListOf()

        // Loop through mCartDetails using index
        for (i in mCartDetails.indices) {
            // For each item, add the required information in the list
            orderItems.add(
                OrderItem(
                    mCartDetails[i].id, mCartDetails[i].name,
                    mCartDetails[i].image, mCartDetails[i].price,
                    mCartDetails[i].unit, mCartDetails[i].weight,
                    mSelectedCartItems[i].prodQTY,
                    mSelectedCartItems[i].prodPrice
                )
            )
        }  // end of for

        // Finally, store the user's Order Items
        orderHashMap[Constants.ORDER_ITEMS] = orderItems

        return orderHashMap  // Return the result
    }  // end of storeOrderValues method

    /* Function to prompt that the user's checkout data is stored in
     * the database, and then proceed to remove the selected items from
     * user's cart
     */
    internal fun orderPlacedPrompt() {
        // Check if mUserInfo and its cart property are not null to prevent NPE
        if (mUserInfo != null && mUserInfo!!.cart != null) {
            /* To determine if the market ID needs to be cleared depending
             * on the capacity of the list. True if cartItems (filtered by
             * selected == false) is empty.
             */
            val clearItems: Boolean = mUserInfo!!.cart!!.cartItems
                .filterNot { it.selected }.isEmpty()

            // Update the cart by either removing some or all items
            FirestoreClass().updateCart(
                this@CheckoutActivity,
                mUserInfo!!.cart!!.cartItems.filterNot { it.selected },
                toClear = clearItems
            )
        }
    }  // end of orderPlacedPrompt method

    // Function to prompt that the cart data was updated and then sends user to home page
    internal fun cartUpdatedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Create an Intent to launch MainActivity
        Intent(this@CheckoutActivity, MainActivity::class.java).apply {
            // To ensure that no more activity layers are active after the user places order
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Displays a Toast message
            toastMessage(
                this@CheckoutActivity,
                getString(R.string.msg_order_placed_success),
                true
            )

            startActivity(this)  // Opens the home page
            finish()  // Closes the activity
        }  // end of apply

    }  // end of cartUpdatedPrompt method

}  // end of CheckoutActivity class
