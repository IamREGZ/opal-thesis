package edu.cccdci.opal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductDashboardAdapter
import edu.cccdci.opal.databinding.FragmentProductsBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.ui.activities.ProductEditorActivity

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var productDBAdapter: ProductDashboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is important to make add icon visible
        setHasOptionsMenu(true)
    }  // end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProductsBinding.inflate(inflater)

        // Temporary data list
        val dataList = mutableListOf<Product>()
        var i = 1
        while (i <= 10) {
            dataList.add(
                Product(
                    name = "Product $i",
                    price = i.toDouble()
                )
            )
            i++
        }

        // Create an object of Product (Vendor Dashboard) Adapter
        productDBAdapter = ProductDashboardAdapter(requireContext(), dataList)

        with(binding) {
            // Sets the adapter of Products RecyclerView
            rvProducts.adapter = productDBAdapter
            // Sets the layout type of the RecyclerView
            rvProducts.layoutManager = GridLayoutManager(context, 2)

            /* If there are no products in the database, display
             * the empty products message
             */
            if (productDBAdapter.itemCount == 0) {
                rvProducts.visibility = View.GONE
                llEmptyProducts.visibility = View.VISIBLE
            }
            // Display the products if there are existing products
            else {
                rvProducts.visibility = View.VISIBLE
                llEmptyProducts.visibility = View.GONE
            }

            return root
        }
    }  // end of onCreateView method

    // Override the function to add plus icon on top app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plus_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }  // end of onCreateOptionsMenu method

    // onOptionsItemSelected events are declared here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Go to Add Products
            R.id.tab_add -> {
                requireContext().startActivity(
                    Intent(requireContext(), ProductEditorActivity::class.java)
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }  // end of onOptionsItemSelected method

}  // end of ProductsFragment class