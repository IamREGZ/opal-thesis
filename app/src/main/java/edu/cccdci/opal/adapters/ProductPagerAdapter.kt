package edu.cccdci.opal.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.cccdci.opal.ui.fragments.ProductInStockFragment
import edu.cccdci.opal.ui.fragments.ProductSoldOutFragment
import edu.cccdci.opal.ui.fragments.ProductUnlistedFragment
import edu.cccdci.opal.ui.fragments.ProductViolationFragment

class ProductPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val productTabsCount: Int,
    private val marketID: String
): FragmentStateAdapter(fragmentActivity) {

    // Function to get the number of items in the tab layout
    override fun getItemCount(): Int = productTabsCount

    // Function to create fragments based on the current position
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ProductInStockFragment(marketID)  // In Stock Products
            1 -> ProductSoldOutFragment(marketID)  // Sold Out Products
            2 -> ProductViolationFragment(marketID)  // Violation Products
            3 -> ProductUnlistedFragment(marketID)  // Unlisted Products
            else -> Fragment()  // Default
        }
    }  // end of createFragment method

}  // end of ProductPagerAdapter class
