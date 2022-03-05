package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.OrderDetailsAdapter
import edu.cccdci.opal.databinding.FragmentOrderDetailsBinding
import edu.cccdci.opal.dataclasses.Order
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class OrderDetailsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter
    private lateinit var mUtility: UtilityClass
    private var mOrderDetails: Order? = null
    private var mVendorStatus: Boolean = false
    private var mOrderItemsCount: Int = 0
    private var mFirstStatus: Int = 0
    private var mSecondStatus: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailsBinding.inflate(inflater)

        // Get the arguments stored by the previous fragment (Order/Sales History)
        val bundle = this.arguments

        // If the bundle has arguments
        if (bundle != null) {
            // Store the order data to mOrderDetails
            mOrderDetails = bundle.getParcelable(Constants.ORDER_DETAILS)
            // Store the boolean value used to decide for layout changes
            mVendorStatus = bundle.getBoolean(Constants.IS_VENDOR, false)
        }

        // To access Android utilities (e.g., Toast, Dialogs, etc.)
        mUtility = UtilityClass()

        // Prevents NullPointerException
        if (mOrderDetails != null) {
            /* Store the total number of items. Default is 0 to prevent errors
             * if the list of order items is empty.
             */
            mOrderItemsCount = if (mOrderDetails!!.orderItems.isNotEmpty())
                mOrderDetails!!.orderItems.sumOf { it.qty }
            else
                0

            // Supply the order status with appropriate status values
            setOrderStatus()
            // Supply the customer's delivery address with appropriate address values
            setDeliveryAddress()
            // Supply the customer's items with appropriate product values
            setOrderItemsData()
            // Supply the rest of order information with appropriate values
            setOrderInformation()
            // Change the CTA buttons' attributes, based on order's status
            setCTAButtons()
        }  // end of if

        with(binding) {
            // Click event for Contact CTA Button
            btnContactCta.setOnClickListener(this@OrderDetailsFragment)
            // Click event for CTA Button 1
            btnOrderCta1.setOnClickListener(this@OrderDetailsFragment)
            // Click event for CTA Button 2
            btnOrderCta2.setOnClickListener(this@OrderDetailsFragment)

            return root
        }
    }  // end of onCreateView method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // TODO: Send user to chat customer/vendor (depending on role)
                R.id.btn_contact_cta -> Toast.makeText(
                    requireContext(), "Clicked Contact CTA", Toast.LENGTH_SHORT
                ).show()

                /* CTA Button 1 and 2's triggers may vary depending on order's
                 * status and customer role
                 */
                R.id.btn_order_cta_1 -> storeOrderUpdate(true)
                R.id.btn_order_cta_2 -> storeOrderUpdate(false)

            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Function to set the order status outputs with the current status values
    private fun setOrderStatus() {
        with(binding) {

            with(Constants) {
                // Store the status image, depending on the status code
                val statusImage = when (mOrderDetails!!.status) {
                    ORDER_PENDING_CODE -> R.drawable.ic_order_pending  // 0
                    ORDER_TO_DELIVER_CODE -> R.drawable.ic_order_to_deliver  // 1
                    ORDER_OFD_CODE -> R.drawable.ic_order_out_for_delivery  // 2
                    ORDER_DELIVERED_CODE -> R.drawable.ic_order_success  // 3
                    ORDER_CANCELLED_CODE -> R.drawable.ic_order_failed  // 4
                    ORDER_RETURN_REQUEST_CODE,
                    ORDER_TO_RETURN_CODE,
                    ORDER_RETURNED_CODE -> R.drawable.ic_order_return  // 5, 6, 7
                    else -> R.drawable.ic_order_unknown
                }
                // Change the Status Image
                ivOdDetStatusIcon.setImageResource(statusImage)

                // Store the status title, depending on the status code
                val statusTitle = when (mOrderDetails!!.status) {
                    ORDER_PENDING_CODE -> R.string.order_pending  // 0
                    ORDER_TO_DELIVER_CODE -> R.string.order_to_deliver  // 1
                    ORDER_OFD_CODE -> R.string.order_out_for_delivery  // 2
                    ORDER_DELIVERED_CODE -> R.string.order_delivered  // 3
                    ORDER_CANCELLED_CODE -> R.string.order_cancelled  // 4
                    ORDER_RETURN_REQUEST_CODE -> R.string.order_return_request  // 5
                    ORDER_TO_RETURN_CODE -> R.string.order_to_return  // 6
                    ORDER_RETURNED_CODE -> R.string.order_returned  // 7
                    else -> R.string.order_unknown
                }
                // Change the Status Title
                tvOdDetStatusTitle.text = resources.getString(statusTitle)
            }  // end of with(Constants)

            // Change the Status Description
            tvOdDetStatusDesc.text = getStatusDescription()
        }  // end of with(binding)

    }  // end of setOrderStatus method

    // Function to get the status description, depending on the status code
    private fun getStatusDescription(): String {
        // String plurals (quantifiers)
        // "item" (mOrderItemsCount == 1), "items" (mOrderItemsCount > 1)
        val item = resources.getQuantityString(
            R.plurals.order_item_quantifier, mOrderItemsCount
        )
        // "is" (mOrderItemsCount == 1), "are" (mOrderItemsCount > 1)
        val isAre = resources.getQuantityString(
            R.plurals.is_are, mOrderItemsCount
        )
        // "has" (mOrderItemsCount == 1), "have" (mOrderItemsCount > 1)
        val hasHave = resources.getQuantityString(
            R.plurals.has_have, mOrderItemsCount
        )
        // "its" (mOrderItemsCount == 1), "their" (mOrderItemsCount > 1)
        val itsTheir = resources.getQuantityString(
            R.plurals.its_their, mOrderItemsCount
        )

        // Get the reason from database. Default is "Unknown"
        val reason = getString(
            R.string.order_fail_reason,
            mOrderDetails!!.reason ?: "Unknown"
        )

        return when (mOrderDetails!!.status) {
            // 0 - Pending
            Constants.ORDER_PENDING_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_pending_desc)
                else  // Customer
                    getString(R.string.cs_od_pending_desc)
            }

            // 1 - To Deliver
            Constants.ORDER_TO_DELIVER_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_to_deliver_desc, item)
                else  // Customer
                    getString(R.string.cs_od_to_deliver_desc, item, isAre)
            }

            // 2 - Out for Delivery
            Constants.ORDER_OFD_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_ofd_desc, item, isAre, itsTheir)
                else  // Customer
                    getString(R.string.cs_od_ofd_desc, item, isAre, itsTheir)
            }

            // 3 - Delivered
            Constants.ORDER_DELIVERED_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_delivered_desc, item, isAre)
                else  // Customer
                    getString(R.string.cs_od_delivered_desc, item, isAre)
            }

            // 4 - Cancelled
            Constants.ORDER_CANCELLED_CODE -> reason  // Both Vendor and Customer

            // 5 - Return/Refund Requested
            Constants.ORDER_RETURN_REQUEST_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_return_req_desc, reason)
                else  // Customer
                    getString(R.string.cs_od_return_req_desc)
            }
            // 6 - To Return/Refund
            Constants.ORDER_TO_RETURN_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_return_acc_desc, item)
                else  // Customer
                    getString(R.string.cs_od_return_acc_desc, item)
            }
            // 7 - Returned/Refunded
            Constants.ORDER_RETURNED_CODE -> {
                if (mVendorStatus)  // Vendor
                    getString(R.string.vd_od_returned_desc, item, hasHave, reason)
                else  // Customer
                    getString(R.string.cs_od_returned_desc, item, hasHave, reason)
            }

            // Unknown
            else -> getString(R.string.order_unknown_desc)  // Both Vendor and Customer
        }  // end of when

    }  // end of getStatusDescription method

    // Function to set the delivery address with the order's inputted address
    private fun setDeliveryAddress() {
        with(binding) {

            with(mOrderDetails!!) {
                // Prevents NullPointerException
                if (address != null) {
                    // Set the Address Contact Info (Full Name and Phone Number)
                    tvOdDetAddrContact.text = getString(
                        R.string.address_contact_info,
                        address.fullName, address.phoneNum
                    )
                    // Set the Detailed Address line
                    tvOdDetAddrLine1.text = mOrderDetails!!.address!!.detailAdd
                    // Set the second address line (barangay, city, province, postal)
                    tvOdDetAddrLine2.text = getString(
                        R.string.addr_line_2,
                        address.barangay, address.city, address.province,
                        address.postal
                    )
                }  // end of if
            }  // end of with(mOrderDetails!!)

        }  // end of with(binding)

    }  // end of setDeliveryAddress method

    // Function to set the order items with appropriate values
    private fun setOrderItemsData() {
        with(binding) {
            // Change the header, depending on user role
            if (mVendorStatus) {
                /* For vendors, change the icon into user and set the
                 * customer's username as title
                 */
                ivOdDetMktCustIcon.setImageResource(R.drawable.ic_user)
                tvOdDetMktCustTitle.text = mOrderDetails!!.custUser
            } else {
                // For customers, just set the market name as title
                tvOdDetMktCustTitle.text = mOrderDetails!!.marketName
            }

            // Sets the layout type of the RecyclerView
            rvOdDetItems.layoutManager = object : LinearLayoutManager(
                requireContext()
            ) {
                // Disable vertical scroll functionality of the RecyclerView
                override fun canScrollVertically(): Boolean = false
            }
            // Create an object of Order Details Adapter
            orderDetailsAdapter = OrderDetailsAdapter(
                requireContext(), this@OrderDetailsFragment,
                mOrderDetails!!.orderItems
            )
            // Sets the adapter of Order Details RecyclerView
            rvOdDetItems.adapter = orderDetailsAdapter

            // For one item, it will display as "1 item", the rest "n items"
            tvOdDetSubtotalLbl.text = resources.getQuantityString(
                R.plurals.subtotal_order_items, mOrderItemsCount, mOrderItemsCount
            )

            // Store the remaining price information of customer's order
            tvOdDetSubtotal.text = getString(
                R.string.item_price, mOrderDetails!!.subtotal
            )
            tvOdDetDeliveryFee.text = getString(
                R.string.item_price, mOrderDetails!!.deliveryFee
            )
            tvOdDetTotalPrice.text = getString(
                R.string.item_price, mOrderDetails!!.totalPrice
            )
        }  // end of with(binding)

    }  // end of setOrderItemsData method

    // Function to set the lower part of order details with appropriate values
    private fun setOrderInformation() {
        with(binding) {
            // Get all the order unavailable actions
            val orderActions = resources.getStringArray(
                R.array.order_unavailable_actions
            )

            with(mOrderDetails!!) {
                // Set the payment method
                tvOdDetPayment.text = payment
                // Set the Order ID
                tvOdDetOrderId.text = id
                // Set the Order Unavailable Action
                tvOdDetOua.text = orderActions[orderAction]

                // Prevents NullPointerException
                if (dates != null) {
                    // Perform the codes if the order placed date is not null
                    if (dates.orderDate != null) {
                        // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                        tvOdDetOrderDate.text = Constants.formatDate(
                            Constants.YMD_HMS24_DATE_FORMAT,
                            dates.orderDate.toDate()
                        )
                        // Make the order date layout visible
                        llOrderDate.visibility = View.VISIBLE
                    }

                    // Perform the codes if the order payment date is not null
                    if (dates.paymentDate != null) {
                        // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                        tvOdDetPaymentDate.text = Constants.formatDate(
                            Constants.YMD_HMS24_DATE_FORMAT,
                            dates.paymentDate!!.toDate()
                        )
                        // Make the payment date layout visible
                        llPaymentDate.visibility = View.VISIBLE
                    }

                    // Perform the codes if the order completion date is not null
                    if (dates.deliverDate != null) {
                        // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                        tvOdDetDeliveryDate.text = Constants.formatDate(
                            Constants.YMD_HMS24_DATE_FORMAT,
                            dates.deliverDate.toDate()
                        )
                        // Make the completed date layout visible
                        llDeliveryDate.visibility = View.VISIBLE
                    }

                    // Perform the codes if the order return date is not null
                    if (dates.returnDate != null) {
                        // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                        tvOdDetReturnDate.text = Constants.formatDate(
                            Constants.YMD_HMS24_DATE_FORMAT,
                            dates.returnDate.toDate()
                        )
                        // Make the return date layout visible
                        llReturnDate.visibility = View.VISIBLE
                    }
                }  // end of if

                // Perform the codes if the special instructions is specified
                if (special.isNotEmpty()) {
                    // Set the Special Instructions
                    tvOdDetSpecIns.text = special

                    // Make the Special Instructions text visible
                    tvOdDetSpecInsLbl.visibility = View.VISIBLE
                    tvOdDetSpecIns.visibility = View.VISIBLE
                }  // end of if
            }  // end of with(mOrderDetails!!)

        }  // end of with(binding)

    }  // end of setOrderInformation method

    // Function to change texts (and appearances) of CTA buttons
    private fun setCTAButtons() {
        with(binding) {
            // Get the contact person for Contact CTA, depending on user's role
            val contactRole = if (mVendorStatus)
                getString(R.string.role_customer)
            else
                getString(R.string.role_vendor)

            /* Set the Contact Button Text. For Customer, "Contact
             * Vendor." For Vendor, "Contact Customer."
             */
            btnContactCta.text = getString(
                R.string.contact_customer_vendor, contactRole
            )

            /* Make the CTA buttons layout visible for status codes 0, 1, 2,
             * 3, 5 and 6
             */
            when (mOrderDetails!!.status) {
                Constants.ORDER_PENDING_CODE,
                Constants.ORDER_TO_DELIVER_CODE,
                Constants.ORDER_OFD_CODE,
                Constants.ORDER_DELIVERED_CODE,
                Constants.ORDER_RETURN_REQUEST_CODE,
                Constants.ORDER_TO_RETURN_CODE -> {
                    // Change the visibility of CTA buttons layout
                    cvMainOrderCtas.visibility = View.VISIBLE

                    // Change the attributes and functionalities of first CTA
                    setFirstCTAButton()
                    // Change the attributes and functionalities of second CTA
                    setSecondCTAButton()
                }
            }  // end of when

        }  // end of with(binding)

    }  // end of setCTAButtons method

    // Function to change the first CTA button's attributes and functionalities
    private fun setFirstCTAButton() {
        val btn1Text: String  // Variable to store the first CTA button's text

        with(Constants) {
            // For Vendors (All status codes except 4 and 7)
            if (mVendorStatus) {
                // Store the string value, depending on the given status codes
                btn1Text = when (mOrderDetails!!.status) {
                    // 0 (Pending) and 1 (To Deliver)
                    ORDER_PENDING_CODE, ORDER_TO_DELIVER_CODE -> {
                        // Change button style to secondary theme
                        toSecondaryButton(binding.btnOrderCta1)
                        // Change status code to 4 (Cancelled)
                        mFirstStatus = ORDER_CANCELLED_CODE

                        // Set the text to "Cancel Order"
                        getString(R.string.cancel_order_btn)
                    }

                    // 2 (Out for Delivery)
                    ORDER_OFD_CODE -> if (mOrderDetails!!.dates!!.paymentDate == null) {
                        // No payment date set

                        // Set the text to "Mark as Paid"
                        getString(R.string.mark_as_paid_btn)
                    } else {
                        // Change status code to 3 (Delivered)
                        mFirstStatus = ORDER_DELIVERED_CODE

                        // Set the text to "Delivered"
                        getString(R.string.order_delivered_btn)
                    }

                    // 3 (Delivered)
                    ORDER_DELIVERED_CODE -> {
                        // Set the icon of the first CTA button
                        binding.btnOrderCta1.setIconResource(R.drawable.ic_rate_star)

                        // TODO: Open the customer's past ratings page


                        // Set the text to "View Customer's Rating"
                        getString(R.string.view_cust_rating_btn)
                    }

                    // 5 (Return/Refund Requested)
                    ORDER_RETURN_REQUEST_CODE -> {
                        // Change status code to 6 (To Return/Refund)
                        mFirstStatus = ORDER_TO_RETURN_CODE

                        // Set the text to "Accept Request"
                        getString(R.string.accept_request_btn)
                    }

                    // 6 (To Return/Refund)
                    ORDER_TO_RETURN_CODE -> {
                        // Change status code to 7 (Returned/Refund)
                        mFirstStatus = ORDER_RETURNED_CODE

                        // Set the text to "Order Returned/Refunded"
                        getString(R.string.order_returned_btn)
                    }

                    else -> ""  // Empty String
                }  // end of when
            }
            // For Customers (Status codes 0, 3 and 5)
            else {
                // Store the string value, depending on the given status codes
                btn1Text = when (mOrderDetails!!.status) {
                    // 0 (Pending)
                    ORDER_PENDING_CODE -> {
                        // Change status code to 4 (Cancelled)
                        mFirstStatus = ORDER_CANCELLED_CODE

                        // Set the text to "Cancel Order"
                        getString(R.string.cancel_order_btn)
                    }

                    // 3 (Delivered)
                    ORDER_DELIVERED_CODE -> {
                        // Change button style to secondary theme
                        toSecondaryButton(binding.btnOrderCta1)
                        // Set the icon of the first CTA button
                        binding.btnOrderCta1.setIconResource(R.drawable.ic_rate_star)

                        // TODO: Open the rate market page


                        // Set the text to "Rate Market"
                        getString(R.string.rate_market_btn)

                        // TODO: View Rating (Open customer's past ratings)


                    }

                    // 5 - Cancel Request
                    ORDER_RETURN_REQUEST_CODE -> {
                        // Change status code to 3 (Delivered)
                        mFirstStatus = ORDER_DELIVERED_CODE

                        // Set the text to "Cancel Request"
                        getString(R.string.cancel_request_btn)
                    }

                    else -> ""  // Empty String
                }  // end of when
            }  // end of if-else

        }  // end of with(Constants)

        // Change the text of the first CTA button
        binding.btnOrderCta1.text = btn1Text
    }  // end of setFirstCTAButton method

    /* Function to make the second CTA button visible and change its attributes
     * and functionalities
     */
    private fun setSecondCTAButton() {
        val btn2Text: String  // Variable to store the second CTA button's text

        with(Constants) {
            // For vendors (Status codes 0, 1, 2 and 6)
            if (mVendorStatus) {
                btn2Text = when (mOrderDetails!!.status) {
                    // 0 (Pending)
                    ORDER_PENDING_CODE -> {
                        // Change status code to 1 (To Deliver)
                        mSecondStatus = ORDER_TO_DELIVER_CODE

                        // Set the text to "Confirm Order"
                        getString(R.string.confirm_order_btn)
                    }

                    // 1 - Start Delivery
                    ORDER_TO_DELIVER_CODE -> {
                        // Change status code to 2 (Out for Delivery)
                        mSecondStatus = ORDER_OFD_CODE

                        // Set the text to "Start Delivery"
                        getString(R.string.start_delivery_btn)
                    }

                    // 2 - Not Delivered (Change style to secondary theme)
                    ORDER_OFD_CODE -> {
                        // Change button style to secondary theme
                        toSecondaryButton(binding.btnOrderCta2)

                        // Change status code to 4 (Cancelled)
                        mSecondStatus = ORDER_CANCELLED_CODE

                        // Set the text to "Not Delivered"
                        getString(R.string.order_not_delivered_btn)
                    }

                    // 5 - Reject Request
                    ORDER_RETURN_REQUEST_CODE -> {
                        // Change status code to 3 (Delivered)
                        mSecondStatus = ORDER_DELIVERED_CODE

                        // Set the text to "Reject Request"
                        getString(R.string.reject_request_btn)
                    }

                    else -> ""  // Empty String
                }  // end of when
            }
            // For customers (Status code 3 only)
            else if (!mVendorStatus &&
                mOrderDetails!!.status == ORDER_DELIVERED_CODE
            ) {
                // Change status code to 5 (Return/Refund Requested)
                mSecondStatus = ORDER_RETURN_REQUEST_CODE

                // Set the text to "Return/Refund"
                btn2Text = getString(R.string.return_refund_btn)

                // TODO: Buy Again


            } else {
                btn2Text = ""  // Empty String
            }  // end of if-else if-else

        }  // end of with(Constants)

        /* Visible to vendors with status codes 0, 1, 2 and 6.
         * To customers, 3 only.
         */
        if (btn2Text.isNotEmpty()) {
            binding.btnOrderCta2.text = btn2Text
            binding.btnOrderCta2.visibility = View.VISIBLE
        }

    }  // end of setSecondCTAButton method

    // Function store fields to update in Firestore database using hash map
    private fun storeOrderUpdate(isFirstCTA: Boolean) {
        // Display the loading message
        mUtility.showProgressDialog(
            requireContext(), requireActivity(), getString(R.string.msg_please_wait)
        )

        // Store the new status code, depending on the CTA button clicked
        val newStatus = if (isFirstCTA) mFirstStatus else mSecondStatus

        // Variable to store the hash map of fields to update
        val orderHM: HashMap<String, Any> = if (
            mOrderDetails!!.status == Constants.ORDER_OFD_CODE &&
            mOrderDetails!!.dates!!.paymentDate == null && isFirstCTA
        ) {
            mFirstStatus = Constants.ORDER_DELIVERED_CODE

            /* For Mark as Paid button, add the current server timestamp
             * to payment date
             */
            hashMapOf(
                "${Constants.DATES}.${Constants.PAYMENT_DATE}" to FieldValue
                    .serverTimestamp()
            )
        } else if (
            mOrderDetails!!.status == Constants.ORDER_OFD_CODE && isFirstCTA
        ) {
            /* For Delivered button, change status code to 3 (Delivered),
             * and add the current server timestamp to delivery date
             */
            hashMapOf(
                Constants.STATUS to newStatus,
                "${Constants.DATES}.${Constants.DELIVER_DATE}" to FieldValue
                    .serverTimestamp()
            )
        } else if (mOrderDetails!!.status == Constants.ORDER_TO_RETURN_CODE) {
            /* For Order Returned/Refund button, change status code to
             * 7 (Return/Refund), and add the current server timestamp to
             * return date
             */
            hashMapOf(
                Constants.STATUS to newStatus,
                "${Constants.DATES}.${Constants.RETURN_DATE}" to FieldValue
                    .serverTimestamp()
            )
        } else {
            // The default hash map value, just change the status code
            hashMapOf(Constants.STATUS to newStatus)
        }

        // Update the order document in Firestore database
        FirestoreClass().updateOrder(
            this@OrderDetailsFragment, mOrderDetails!!.id, orderHM, mUtility,
            isFirstCTA
        )
    }  // end of storeOrderUpdate method

    // Function to prompt that the order data was updated
    fun orderUpdatedPrompt(isFirstCTA: Boolean) {
        // Hide the loading message
        mUtility.hideProgressDialog()

        // If the status code is 2 and payment date is not set (Mark as Paid button)
        if (mOrderDetails!!.status == Constants.ORDER_OFD_CODE &&
            mOrderDetails!!.dates!!.paymentDate == null && isFirstCTA
        ) {
            // Store the temporary timestamp
            mOrderDetails!!.dates!!.paymentDate = Timestamp.now()
            // Change the first CTA button's text to Delivered
            binding.btnOrderCta1.text = getString(R.string.order_delivered_btn)
        } else {
            requireActivity().onBackPressed()  // Sends user back to previous fragment
        }  // end of if-else

    }  // end of orderUpdatedPrompt method

}  // end of OrderDetailsFragment class
