package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.databinding.ActivityCartBinding
import edu.cccdci.opal.dataclasses.Cart
import edu.cccdci.opal.utils.UtilityClass

class CartActivity : UtilityClass() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMyCartActivity, false)

            // Temporary data list
            val dataList = mutableListOf<Cart>()
            var i = 1
            while (i <= 5) {
                dataList.add(
                    Cart(
                        name = "Product $i",
                        price = i.toDouble(),
                    )
                )
                i++
            }

            // Create an object of Cart Adapter
            cartAdapter = CartAdapter(dataList)
            // Sets the adapter of Cart RecyclerView
            rvCart.adapter = cartAdapter
            // Sets the layout type of the RecyclerView
            rvCart.layoutManager = LinearLayoutManager(this@CartActivity)
        }  // end of with(binding)

    }  // end of onCreate method

}  // end of CartActivity class