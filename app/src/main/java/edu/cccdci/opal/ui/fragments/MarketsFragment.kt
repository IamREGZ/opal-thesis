package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import edu.cccdci.opal.adapters.MarketAdapter
import edu.cccdci.opal.databinding.FragmentMarketsBinding
import edu.cccdci.opal.dataclasses.Market

class MarketsFragment : Fragment() {

    private lateinit var binding: FragmentMarketsBinding
    private lateinit var marketAdapter: MarketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMarketsBinding.inflate(inflater)

        val dataList = mutableListOf<Market>()

        var i = 1
        while (i <= 10) {
            dataList.add(Market(name = "Market $i", address = "Address $i"))
            i++
        }

        marketAdapter = MarketAdapter(dataList)

        with(binding) {
            rvMarkets.adapter = marketAdapter
            rvMarkets.layoutManager = GridLayoutManager(context, 2)

            if (marketAdapter.itemCount == 0) {
                rvMarkets.visibility = View.GONE
                llEmptyMarkets.visibility = View.VISIBLE
            } else {
                rvMarkets.visibility = View.VISIBLE
                llEmptyMarkets.visibility = View.GONE
            }

            return root
        }
    } //end of onCreateView method

} //end of MarketsFragment class