package edu.cccdci.opal.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.ui.activities.ProductEditorActivity
import edu.cccdci.opal.utils.Constants

class ProductDashboardAdapter(
    val context: Context,
    private val productDBDataList: MutableList<Product>
) : RecyclerView.Adapter<ProductDashboardAdapter.ProductDashboardViewHolder>() {

    class ProductDashboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.prod_db_name)
        val price: TextView = itemView.findViewById(R.id.prod_db_price)
        val productCard: CardView = itemView.findViewById(R.id.cv_product_db)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProductDashboardViewHolder =
        ProductDashboardViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.product_dashboard_card, parent, false
            )
        )

    override fun onBindViewHolder(holder: ProductDashboardViewHolder, position: Int) {
        val productData = productDBDataList[position]

        holder.name.text = productData.name
        holder.price.text = "₱${productData.price} / kg"

        holder.productCard.setOnClickListener {
            //Create an Intent to launch ProductDescActivity
            val intent = Intent(
                context, ProductEditorActivity::class.java
            )
            //Add product information to intent
            intent.putExtra(Constants.PRODUCT_DESCRIPTION, productData)

            //Opens the edit user profile
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productDBDataList.size

}