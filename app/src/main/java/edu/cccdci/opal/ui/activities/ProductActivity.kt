package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductPagerAdapter
import edu.cccdci.opal.databinding.ActivityProductBinding
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class ProductActivity : UtilityClass() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var mProductPager: ProductPagerAdapter
    private var mMarketID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbProductActivity, false)

            // Check if there's an existing string extra info
            if (intent.hasExtra(Constants.MARKET_ID_DATA)) {
                // Get the string data from intent
                mMarketID = intent.getStringExtra(Constants.MARKET_ID_DATA)
            }

            // Get the string array of product tabs
            val productTitles = resources.getStringArray(R.array.product_tabs)

            // Create an object of Product Pager Adapter
            mProductPager = ProductPagerAdapter(
                this@ProductActivity, productTitles.size
            )
            // Set the adapter of Product List ViewPager2
            vpProductList.adapter = mProductPager

            // Integrates the tab items with respective fragments
            TabLayoutMediator(tblyProductTabs, vpProductList) { tab, position ->
                tab.text = productTitles[position]
            }.attach()

            // Actions taken when the Add New Product button is clicked
            btnAddProduct.setOnClickListener {
                val intent = Intent(
                    this@ProductActivity,
                    ProductEditorActivity::class.java
                )

                intent.putExtra(Constants.MARKET_ID_DATA, mMarketID ?: "")

                // Go to Product Editor Activity
                startActivity(intent)
            }

        }  // end of with(binding)

    }  // end of onCreate method

}  // end of ProductActivity class