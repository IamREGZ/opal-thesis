package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.OrderDetailsAdapter
import edu.cccdci.opal.databinding.FragmentOrderDetailsBinding
import edu.cccdci.opal.dataclasses.Order
import edu.cccdci.opal.utils.Constants

class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter
    private var mOrderDetails: Order? = null
    private var mVendorStatus: Boolean = false
    private var mOrderItemsCount: Int = 0

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

        // Prevents NullPointerException
        if (mOrderDetails != null) {
            // Supply the order status with appropriate status values
            setOrderStatus()
            // Supply the customer's delivery address with appropriate address values
            setDeliveryAddress()
            // Supply the customer's items with appropriate product values
            setOrderItemsData()
            // Supply the rest of order information with appropriate values
            setOrderInformation()
        }  // end of if

        return binding.root
    }  // end of onCreateView method

    // Function to set the order status outputs with the current status values
    private fun setOrderStatus() {
        with(binding) {
            // Prevents NullPointerException
            if (mOrderDetails!!.status != null) {
                // Store the status image, depending on the status code
                val statusImage = when (mOrderDetails!!.status!!.code) {
                    0 -> R.drawable.ic_order_pending
                    1 -> R.drawable.ic_order_out_for_delivery
                    2 -> R.drawable.ic_order_success
                    3 -> R.drawable.ic_order_failed
                    4 -> R.drawable.ic_order_return
                    5 -> R.drawable.ic_order_failed
                    else -> R.drawable.ic_order_unknown
                }

                // Change the Status Image
                ivOdDetStatusIcon.setImageResource(statusImage)

                // Change the Status Title and Description
                tvOdDetStatusTitle.text = mOrderDetails!!.status!!.title
                tvOdDetStatusDesc.text = mOrderDetails!!.status!!.description
            }  // end of if
        }  // end of with(binding)

    }  // end of setOrderStatus method

    // Function to set the delivery address with the order's inputted address
    private fun setDeliveryAddress() {
        with(binding) {
            // Prevents NullPointerException
            if (mOrderDetails!!.address != null) {
                // Set the Address Contact Info (Full Name and Phone Number)
                tvOdDetAddrContact.text = getString(
                    R.string.address_contact_info,
                    mOrderDetails!!.address!!.fullName,
                    mOrderDetails!!.address!!.phoneNum
                )
                // Set the Detailed Address line
                tvOdDetAddrLine1.text = mOrderDetails!!.address!!.detailAdd
                // Set the second address line (barangay, city, province, postal)
                tvOdDetAddrLine2.text = getString(
                    R.string.addr_line_2,
                    mOrderDetails!!.address!!.barangay,
                    mOrderDetails!!.address!!.city,
                    mOrderDetails!!.address!!.province,
                    mOrderDetails!!.address!!.postal
                )
            }  // end of if
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
                requireContext(), mOrderDetails!!.orderItems
            )
            // Sets the adapter of Order Details RecyclerView
            rvOdDetItems.adapter = orderDetailsAdapter

            /* Store the total number of items. Default is 0 to prevent errors
             * if the list of order items is empty.
             */
            mOrderItemsCount = if (mOrderDetails!!.orderItems.isNotEmpty())
                mOrderDetails!!.orderItems.sumOf { it.qty }
            else
                0

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
            // Set the payment method
            tvOdDetPayment.text = mOrderDetails!!.payment
            // Set the Order ID
            tvOdDetOrderId.text = mOrderDetails!!.id
            // Set the Order Unavailable Action
            tvOdDetOua.text = mOrderDetails!!.orderAction

            // Prevents NullPointerException
            if (mOrderDetails!!.dates != null) {
                // Perform the codes if the order placed date is not null
                if (mOrderDetails!!.dates!!.orderDate != null) {
                    // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                    tvOdDetOrderDate.text = Constants.formatDate(
                        Constants.YMD_HMS24_DATE_FORMAT,
                        mOrderDetails!!.dates!!.orderDate!!.toDate()
                    )
                    // Make the order date layout visible
                    llOrderDate.visibility = View.VISIBLE
                }

                // Perform the codes if the order payment date is not null
                if (mOrderDetails!!.dates!!.paymentDate != null) {
                    // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                    tvOdDetPaymentDate.text = Constants.formatDate(
                        Constants.YMD_HMS24_DATE_FORMAT,
                        mOrderDetails!!.dates!!.orderDate!!.toDate()
                    )
                    // Make the payment date layout visible
                    llPaymentDate.visibility = View.VISIBLE
                }

                // Perform the codes if the order completion date is not null
                if (mOrderDetails!!.dates!!.completeDate != null) {
                    // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                    tvOdDetCompletedDate.text = Constants.formatDate(
                        Constants.YMD_HMS24_DATE_FORMAT,
                        mOrderDetails!!.dates!!.orderDate!!.toDate()
                    )
                    // Make the completed date layout visible
                    llCompletedDate.visibility = View.VISIBLE
                }

                // Perform the codes if the order return date is not null
                if (mOrderDetails!!.dates!!.returnDate != null) {
                    // Set the date using this format (yyyy-MM-dd HH:mm:ss)
                    tvOdDetReturnDate.text = Constants.formatDate(
                        Constants.YMD_HMS24_DATE_FORMAT,
                        mOrderDetails!!.dates!!.orderDate!!.toDate()
                    )
                    // Make the return date layout visible
                    llReturnDate.visibility = View.VISIBLE
                }
            }  // end of if

            // Perform the codes if the special instructions is specified
            if (mOrderDetails!!.special.isNotEmpty()) {
                // Set the Special Instructions
                tvOdDetSpecIns.text = mOrderDetails!!.special

                // Make the Special Instructions text visible
                tvOdDetSpecInsLbl.visibility = View.VISIBLE
                tvOdDetSpecIns.visibility = View.VISIBLE
            }  // end of if
        }  // end of with(binding)

    }  // end of setOrderInformation method

}  // end of OrderDetailsFragment class