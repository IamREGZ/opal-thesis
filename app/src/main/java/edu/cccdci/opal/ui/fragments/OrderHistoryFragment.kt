package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.OrderSalesPagerAdapter
import edu.cccdci.opal.databinding.FragmentOrderHistoryBinding

class OrderHistoryFragment : Fragment() {

    private lateinit var binding: FragmentOrderHistoryBinding
    private lateinit var mOrderPager: OrderSalesPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderHistoryBinding.inflate(inflater)

        with(binding) {
            // Get the string array of order tabs
            val orderTitles = resources.getStringArray(R.array.order_sales_tabs)

            // Create an object of Order Pager Adapter
            mOrderPager = OrderSalesPagerAdapter(
                requireActivity(), orderTitles.size,
            )
            // Set the adapter of Order List ViewPager2
            vpOrderList.adapter = mOrderPager

            // Integrates the tab items with respective fragments
            TabLayoutMediator(tblyOrderTabs, vpOrderList) { tab, position ->
                tab.text = orderTitles[position]
            }.attach()

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

}  // end of OrderHistoryFragment class