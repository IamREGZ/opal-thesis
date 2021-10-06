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
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.ui.activities.ProductDescActivity
import edu.cccdci.opal.utils.Constants

class ProductAdapter(
    val context: Context,
    private val productDataList: MutableList<Product>
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val market: TextView = itemView.findViewById(R.id.product_market)
        val productCard: CardView = itemView.findViewById(R.id.cv_product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
        ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.product_card, parent, false
            )
        )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val productData = productDataList[position]
        val user = User()

        holder.name.text = productData.name
        holder.price.text = "₱${productData.price} / kg"
        holder.market.text = productData.market

        holder.productCard.setOnClickListener {
            //Create an Intent to launch ProductDescActivity
            val intent = Intent(
                context, ProductDescActivity::class.java
            )
            //Add product information to intent
            intent.putExtra(Constants.PRODUCT_DESCRIPTION, productData)
            intent.putExtra(Constants.EXTRA_USER_INFO, user)

            //Opens the edit user profile
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productDataList.size


}