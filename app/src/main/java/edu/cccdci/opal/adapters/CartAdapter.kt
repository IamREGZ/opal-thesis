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
): RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {
        val name: TextView = itemView.findViewById(R.id.cart_prod_name)
        val price: TextView = itemView.findViewById(R.id.cart_prod_price)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cb_select_item)
        val delete: ImageView = itemView.findViewById(R.id.del_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder =
        CartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cart_item, parent, false
            )
        )

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartData = cartDataList[position]

        with(holder) {
            name.text = cartData.name
            price.text = "₱${cartData.price} / kg"
            cbSelect.isChecked = cartData.isSelected
            toggleDeleteButton(delete, cartData.isSelected)

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                toggleDeleteButton(delete, isChecked)
                cartData.isSelected = !cartData.isSelected
            }

            delete.setOnClickListener {
                cartDataList.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = cartDataList.size

    private fun toggleDeleteButton(delBtn: ImageView, isSelected: Boolean) {
        if (isSelected)
            delBtn.visibility = View.VISIBLE
        else
            delBtn.visibility = View.GONE
    }

}