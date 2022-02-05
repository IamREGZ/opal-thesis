package edu.cccdci.opal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.OrderItem
import edu.cccdci.opal.utils.GlideLoader

class OrderDetailsAdapter(
    private val context: Context,
    private val orderItemList: List<OrderItem>
) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    // Nested Class to hold views from the target layout
    inner class OrderDetailsViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from order item layout
        private val odProdName: TextView = itemView
            .findViewById(R.id.tv_od_det_prod_name)
        private val odProdPrice: TextView = itemView
            .findViewById(R.id.tv_od_det_prod_price)
        private val odProdQTY: TextView = itemView
            .findViewById(R.id.tv_od_det_prod_qty)
        private val odProdTotal: TextView = itemView
            .findViewById(R.id.tv_od_det_prod_total_price)
        private val odProdImage: ImageView = itemView
            .findViewById(R.id.iv_od_det_prod_image)

        // Function to provide the product details in the current order item
        internal fun setOrderItemData(item: OrderItem) {
            // Supply the following text views
            odProdName.text = item.name
            odProdPrice.text = context.getString(
                R.string.product_price, item.price, item.unit
            )
            odProdQTY.text = context.getString(
                R.string.product_qty, item.qty
            )
            odProdTotal.text = context.getString(
                R.string.item_price, item.totalPrice
            )

            // Add also the product image
            GlideLoader(context).loadImage(item.image, odProdImage)
        }  // end of setOrderItemData method

    }  // end of OrderDetailsViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OrderDetailsViewHolder {
        return OrderDetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.order_details_item, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        // Get an object from the current position of order item list
        val orderItem = orderItemList[position]

        // Sets the values of order item data to the current view
        holder.setOrderItemData(orderItem)
    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = orderItemList.size

}  // end of OrderDetailsAdapter class