package edu.cccdci.opal.adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Order
import edu.cccdci.opal.dataclasses.OrderItem
import edu.cccdci.opal.utils.Constants

class OrderSalesAdapter(
    private val fragment: Fragment,
    private val context: Context,
    private val isVendor: Boolean,
    options: FirestoreRecyclerOptions<Order>
) : FirestoreRecyclerAdapter<Order, OrderSalesAdapter.OrderSalesViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class OrderSalesViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from order/sales item layout
        private val odsHead: TextView = itemView
            .findViewById(R.id.tv_order_title_head)
        private val odsID: TextView = itemView
            .findViewById(R.id.tv_order_id)
        private val odsDate: TextView = itemView
            .findViewById(R.id.tv_order_date)
        private val odsItems: TextView = itemView
            .findViewById(R.id.tv_order_items)
        private val odsItemQTY: TextView = itemView
            .findViewById(R.id.tv_order_items_qty)
        private val odsTotal: TextView = itemView
            .findViewById(R.id.tv_order_total_price)
        private val odsStatus: TextView = itemView
            .findViewById(R.id.tv_order_status_title)
        private val odsHeadIcon: ImageView = itemView
            .findViewById(R.id.iv_order_icon)
        private val odsStatusIcon: ImageView = itemView
            .findViewById(R.id.iv_order_status_icon)
        private val odsOrderPanel: LinearLayout = itemView
            .findViewById(R.id.ll_order_panel)

        // Function to set values to the specified views from order/sales item
        internal fun setOrderSalesData(order: Order) {
            /* Use Market Name for Order History, Customer's Username
             * for Sales History
             */
            odsHead.text = if (isVendor) order.custUser else order.marketName

            /* Change the order head icon when the item's parent view
             * is Sales History
             */
            if (isVendor) odsHeadIcon.setImageResource(R.drawable.ic_user)

            // Store the order ID
            odsID.text = order.id
            // Get the String sequence of order items
            odsItems.text = getOrderListSequence(order.orderItems)

            /* Get the total number of items; has default value of 0
             * to prevent any errors in getting the sum of each QTY.
             */
            val totalItems = if (order.orderItems.isNotEmpty())
                order.orderItems.sumOf { it.qty }
            else
                0

            // For one item, it will display as "1 item", the rest "n items"
            odsItemQTY.text = context.resources.getQuantityString(
                R.plurals.items_quantity, totalItems, totalItems
            )
            // Store the total price of an order
            odsTotal.text = context.getString(
                R.string.item_price, order.totalPrice
            )

            // Set the exact order date if order.dates is not null
            odsDate.text = if (order.dates != null)
                getOrderDateText(order.dates.orderDate)
            // The default value (MM/dd/yyyy hh:mm a)
            else
                Constants.MDY_HM12_DATE_FORMAT

            // Set the icon and color of order's status
            setStatusAttributes(order.status)

            // Actions when Order Panel Layout is clicked
            odsOrderPanel.setOnClickListener { goToOrderDetails(order) }
        }  // end of setOrderData method

        // Function to set the icons and colors of order's status
        private fun setStatusAttributes(orderStatus: Int) {
            // Store the icon ID depending on the Order's Status Code
            val iconID = when (orderStatus) {
                // 0, 1, 2
                Constants.ORDER_PENDING_CODE,
                Constants.ORDER_TO_DELIVER_CODE,
                Constants.ORDER_OFD_CODE -> R.drawable.ic_status_in_progress
                // 3
                Constants.ORDER_DELIVERED_CODE -> R.drawable.ic_status_success
                // 4
                Constants.ORDER_CANCELLED_CODE -> R.drawable.ic_status_cancelled
                // 5, 6, 7
                Constants.ORDER_RETURN_REQUEST_CODE,
                Constants.ORDER_TO_RETURN_CODE,
                Constants.ORDER_RETURNED_CODE -> R.drawable.ic_status_return
                else -> R.drawable.ic_status_info
            }

            // Store the text color depending on the Order's Status Code
            val statusColor = when (orderStatus) {
                // 0, 1, 2
                Constants.ORDER_PENDING_CODE,
                Constants.ORDER_TO_DELIVER_CODE,
                Constants.ORDER_OFD_CODE -> Constants.MEDIUM_ORANGE  // #FFF28500
                // 3
                Constants.ORDER_DELIVERED_CODE -> Constants.APP_DARK_GREEN  // #FF014421
                // 4
                Constants.ORDER_CANCELLED_CODE -> Constants.DIM_GRAY  // #FF696969
                // 5, 6, 7
                Constants.ORDER_RETURN_REQUEST_CODE,
                Constants.ORDER_TO_RETURN_CODE,
                Constants.ORDER_RETURNED_CODE -> Constants.APP_DARK_TEAL  // #FF006666
                else -> Constants.APP_BLACK  // #FF0F0F0F
            }

            // Store the status name depending on the Order's Status Code
            val statusName = when (orderStatus) {
                Constants.ORDER_PENDING_CODE -> R.string.order_pending  // 0
                Constants.ORDER_TO_DELIVER_CODE -> R.string.order_to_deliver  // 1
                Constants.ORDER_OFD_CODE -> R.string.order_out_for_delivery  // 2
                Constants.ORDER_DELIVERED_CODE -> R.string.order_delivered  // 3
                Constants.ORDER_CANCELLED_CODE -> R.string.order_cancelled  // 4
                Constants.ORDER_RETURN_REQUEST_CODE -> R.string.order_return_request  // 5
                Constants.ORDER_TO_RETURN_CODE -> R.string.order_to_return  // 6
                Constants.ORDER_RETURNED_CODE -> R.string.order_returned  // 7
                else -> R.string.order_unknown
            }

            // Change the icon of Order's Status
            odsStatusIcon.setImageResource(iconID)
            // Change the text color of Order's Status
            odsStatus.setTextColor(Color.parseColor(statusColor))
            // Store the current status of the order/sale. Default is Unknown.
            odsStatus.text = context.resources.getString(statusName)
        }  // end of setStatusAttributes method

        // Function to get the formatted order date as String
        private fun getOrderDateText(orderDate: Timestamp?): String {
            // Return the formatted date if orderDate is not null
            return if (orderDate != null) {
                Constants.formatDate(
                    Constants.MDY_HM12_DATE_FORMAT, orderDate.toDate()
                )
            }
            // The default value (MM/dd/yyyy hh:mm a)
            else {
                Constants.MDY_HM12_DATE_FORMAT
            }
        }  // end of getOrderDateText method

        // Function to convert the list of order items into String sequence
        private fun getOrderListSequence(orderList: List<OrderItem>): String {
            // The list of order items written in String sequence
            var itemSequence = ""

            // Loop through the list using index
            for (index in orderList.indices) {
                // Format: OrderItemName (xQTY)
                itemSequence += "${orderList[index].name} " +
                        "(x${orderList[index].qty})"

                // Add a comma if it is not the last item
                if (index != orderList.size - 1) itemSequence += ", "
            }  // end of for

            // Return the final output of sequence
            return itemSequence.ifEmpty {
                context.resources.getString(R.string.empty_order_items)
            }
        }  // end of getOrderListSequence method

        // Function to send user to Order Details Page
        private fun goToOrderDetails(order: Order) {
            // Create a bundle object to store the order object
            Bundle().run {
                // Stores the parcelable class
                putParcelable(Constants.ORDER_DETAILS, order)
                // Stores the boolean value (for changes in Order Details layout)
                putBoolean(Constants.IS_VENDOR, isVendor)

                // Send user to Order Details Fragment with the bundle object
                fragment.findNavController().navigate(
                    R.id.order_to_order_details, this
                )
            }
        }  // end of goToOrderDetails method

    }  // end of OrderSalesViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OrderSalesViewHolder {
        return OrderSalesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_order_card, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: OrderSalesViewHolder, position: Int, order: Order
    ) {
        // Sets the values of order/sales data to the current view
        holder.setOrderSalesData(order)
    }  // end of onBindViewHolder method

}  // end of OrderSalesAdapter class
