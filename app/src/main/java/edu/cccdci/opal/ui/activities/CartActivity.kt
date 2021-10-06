package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.databinding.ActivityCartBinding
import edu.cccdci.opal.dataclasses.Cart
import edu.cccdci.opal.dataclasses.User

class CartActivity : TemplateActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var mUserInfo: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            setupActionBar(tlbMyCartActivity, false)

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

            cartAdapter = CartAdapter(dataList)

            rvCart.adapter = cartAdapter
            rvCart.layoutManager = LinearLayoutManager(this@CartActivity)
        }
    }
}