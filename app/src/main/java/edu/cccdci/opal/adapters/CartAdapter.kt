package edu.cccdci.opal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Cart

class CartAdapter(
    private val cartDataList: MutableList<Cart>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Nested Class to hold views from the target layout
    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.cart_prod_name)
        val price: TextView = itemView.findViewById(R.id.cart_prod_price)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cb_select_item)
        val delete: ImageView = itemView.findViewById(R.id.del_button)
    }  // end of CartViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cart_item, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        // Get the object from the current position of the data list
        val cartData = cartDataList[position]

        with(holder) {
            // Store the cartData values in the respective views
            name.text = cartData.name
            price.text = "₱${cartData.price} / kg"
            cbSelect.isChecked = cartData.isSelected

            // Sets the initial visibility of delete button
            toggleDeleteButton(delete, cartData.isSelected)

            // Actions when the checkbox is selected
            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                // Change the visibility of delete button
                toggleDeleteButton(delete, isChecked)
                cartData.isSelected = !cartData.isSelected
            }

            // Actions when the delete button is clicked
            delete.setOnClickListener {
                // Delete the current item from the RecyclerView
                cartDataList.removeAt(position)
                notifyDataSetChanged()
            }
        }  // end of with(holder)

    }  // end of onBindViewHolder method

    // Function to get the number of items in the RecyclerView
    override fun getItemCount(): Int = cartDataList.size

    // Function to display delete button when checkbox is checked
    private fun toggleDeleteButton(delBtn: ImageView, isSelected: Boolean) {
        if (isSelected)
            delBtn.visibility = View.VISIBLE
        else
            delBtn.visibility = View.GONE
    }  // end of toggleDeleteButton method

}  // end of CartAdapter class