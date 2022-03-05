package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.adapters.OrderSalesAdapter
import edu.cccdci.opal.databinding.FragmentOrderToDeliverBinding
import edu.cccdci.opal.dataclasses.Order
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager

class OrderToDeliverFragment(private val isVendor: Boolean) : Fragment() {

    private lateinit var binding: FragmentOrderToDeliverBinding
    private var orderSalesAdapter: OrderSalesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderToDeliverBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Order>()
            // Gets all the documents from orders collection
            .setQuery(
                FirestoreClass().getOrderQuery(
                    this@OrderToDeliverFragment, isVendor
                ),
                Order::class.java
            ).build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvOrderToDeliver.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Order/Sales History Adapter
            orderSalesAdapter = OrderSalesAdapter(
                this@OrderToDeliverFragment, requireContext(), isVendor,
                options
            )
            // Sets the adapter of Order/Sales History (To Deliver) RecyclerView
            rvOrderToDeliver.adapter = orderSalesAdapter

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

}  // end of OrderToDeliverFragment class
