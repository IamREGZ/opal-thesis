package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.OrderSalesPagerAdapter
import edu.cccdci.opal.databinding.FragmentSalesHistoryBinding

class SalesHistoryFragment : Fragment() {

    private lateinit var binding: FragmentSalesHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSalesHistoryBinding.inflate(inflater)

        with(binding) {
            // Get the string array of sales tabs
            val salesTitles = resources.getStringArray(R.array.order_sales_tabs)

            // Set the adapter of Sales List ViewPager2
            vpSalesList.adapter = OrderSalesPagerAdapter(
                requireActivity(), salesTitles.size, true
            )

            // Integrates the tab items with respective fragments
            TabLayoutMediator(tblySalesTabs, vpSalesList) { tab, position ->
                tab.text = salesTitles[position]
            }.attach()

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

}  // end of SalesHistoryFragment class
