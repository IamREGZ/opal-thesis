package edu.cccdci.opal.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.cccdci.opal.ui.fragments.*

class OrderSalesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val orderTabsCount: Int,
    private val isVendor: Boolean = false
): FragmentStateAdapter(fragmentActivity) {

    // Function to get the number of items in the tab layout
    override fun getItemCount(): Int = orderTabsCount

    // Function to create fragments based on the current position
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> OrderPendingFragment(isVendor)
            1 -> OrderToDeliverFragment(isVendor)
            2 -> OrderDeliveredFragment(isVendor)
            3 -> OrderCancelledFragment(isVendor)
            4 -> OrderReturnFragment(isVendor)
            else -> Fragment()
        }
    }  // end of createFragment method

}  // end of ProductPagerAdapter class
