package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.adapters.OrderSalesAdapter
import edu.cccdci.opal.databinding.FragmentOrderFailedBinding
import edu.cccdci.opal.dataclasses.Order
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager

class OrderFailedFragment(
    private val isVendor: Boolean
) : Fragment() {

    private lateinit var binding: FragmentOrderFailedBinding
    private var orderSalesAdapter: OrderSalesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderFailedBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Order>()
            // Gets all the documents from orders collection
            .setQuery(
                FirestoreClass().getOrderQuery(
                    this@OrderFailedFragment, isVendor
                ),
                Order::class.java
            ).build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvOrderFailed.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Order/Sales History Adapter
            orderSalesAdapter = OrderSalesAdapter(
                this@OrderFailedFragment, requireContext(), isVendor,
                options
            )
            // Sets the adapter of Order/Sales History (Failed) RecyclerView
            rvOrderFailed.adapter = orderSalesAdapter

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on orders
        orderSalesAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onDestroyView() {
        super.onDestroyView()

        // Stops listening to Firestore operations on orders
        if (orderSalesAdapter != null)
            orderSalesAdapter!!.stopListening()
    }  // end of onDestroyView method

}  // end of OrderFailedFragment class