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
    private val productTabsCount: Int
): FragmentStateAdapter(fragmentActivity) {

    // Function to get the number of items in the tab layout
    override fun getItemCount(): Int = productTabsCount

    // Function to create fragments based on the current position
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ProductInStockFragment()  // In Stock Products
            1 -> ProductSoldOutFragment()  // Sold Out Products
            2 -> ProductViolationFragment()  // Violation Products
            3 -> ProductUnlistedFragment()  // Unlisted Products
            else -> Fragment()  // Default
        }
    }  // end of createFragment method

}  // end of ProductPagerAdapter class
