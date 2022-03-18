package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.adapters.ProductInventoryAdapter
import edu.cccdci.opal.databinding.FragmentProductUnlistedBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperLinearLayoutManager

class ProductUnlistedFragment(private val marketID: String) : Fragment() {

    private lateinit var binding: FragmentProductUnlistedBinding
    private var productInvAdapter: ProductInventoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Inflate the layout for this fragment
        binding = FragmentProductUnlistedBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Product>()
            // Gets all the documents from products collection
            .setQuery(
                FirestoreClass().getProductQuery(
                    this@ProductUnlistedFragment, marketID = marketID
                ),
                Product::class.java
            ).build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvProductUnlisted.layoutManager = WrapperLinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )

            // Create an object of Product Inventory Adapter
            productInvAdapter = ProductInventoryAdapter(
                this@ProductUnlistedFragment, requireContext(),
                requireActivity(), options
            )
            // Sets the adapter of Product Inventory (Violation) RecyclerView
            rvProductUnlisted.adapter = productInvAdapter

            return root
        }
    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Starts listening to Firestore operations on products
        productInvAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onDestroyView() {
        super.onDestroyView()

        // Stops listening to Firestore operations on products
        if (productInvAdapter != null)
            productInvAdapter!!.stopListening()
    }  // end of onDestroyView method

}  // end of ProductUnlistedFragment method
