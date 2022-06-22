package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cccdci.opal.adapters.MarketAdapter
import edu.cccdci.opal.databinding.FragmentMarketsBinding
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager

class MarketsFragment : Fragment() {

    private lateinit var binding: FragmentMarketsBinding
    private var marketAdapter: MarketAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMarketsBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
//        val options = FirestoreRecyclerOptions.Builder<Market>()
//            // Gets all the documents from markets collection
//            .setQuery(FirestoreClass().getMarketQuery(), Market::class.java)
//            .build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvMarkets.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

//            // Create an object of Market Adapter
//            marketAdapter = MarketAdapter(
//                requireContext(), this@MarketsFragment, options
//            )

            // Sets the adapter of Markets RecyclerView
            rvMarkets.adapter = marketAdapter

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on markets
        // marketAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onDestroyView() {
        super.onDestroyView()

        // Stops listening to Firestore operations on markets
//        if (marketAdapter != null)
//            marketAdapter!!.stopListening()
    }  // end of onDestroyView method

}  // end of MarketsFragment class