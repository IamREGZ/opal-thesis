package edu.cccdci.opal.ui.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductPagerAdapter
import edu.cccdci.opal.databinding.ActivityProductBinding
import edu.cccdci.opal.utils.UtilityClass

class ProductActivity : UtilityClass() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var mProductPager: ProductPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbProductActivity, false)

            // Get the string array of product tabs
            val productTitles = resources.getStringArray(R.array.product_tabs).toList()

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
                // Go to Product Editor Activity
                startActivity(
                    Intent(
                        this@ProductActivity,
                        ProductEditorActivity::class.java
                    )
                )
            }

        }  // end of with(binding)

    }  // end of onCreate method

}  // end of ProductActivity class