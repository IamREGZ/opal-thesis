package edu.cccdci.opal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductAdapter
import edu.cccdci.opal.databinding.FragmentHomeBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.layoutwrapper.WrapperGridLayoutManager
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.utils.Constants

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var mUserInfo: User? = null
    private var productAdapter: ProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is important to make two icons (my cart and messages) visible
        setHasOptionsMenu(true)
    }  // end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        // Create a Builder for FirestoreRecyclerOptions
        val options = FirestoreRecyclerOptions.Builder<Product>()
            // Gets all the documents from products collection
            .setQuery(
                FirestoreClass().getProductQuery(this@HomeFragment),
                Product::class.java
            )
            .build()

        with(binding) {
            // Sets the layout type of the RecyclerView
            rvHome.layoutManager = WrapperGridLayoutManager(
                requireContext(), 2, GridLayoutManager.VERTICAL, false
            )

            // Create an object of Product Adapter
            productAdapter = ProductAdapter(
                requireContext(), this@HomeFragment, options
            )

            // Sets the adapter of Home RecyclerView
            rvHome.adapter = productAdapter

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // Operations to do when the fragment is visible
    override fun onStart() {
        super.onStart()

        // Get the argument from the parent activity (MainActivity)
        val bundle = this.arguments

        /* Get the parcelable class (User) from the parent activity
         * (MainActivity)
         */
        if (bundle != null)
            mUserInfo = bundle.getParcelable(Constants.EXTRA_USER_INFO)

        // Starts listening to Firestore operations on products
        productAdapter!!.startListening()
    }  // end of onStart method

    // Operations to do when the fragment is no longer visible
    override fun onDestroyView() {
        super.onDestroyView()

        // Stops listening to Firestore operations on products
        if (productAdapter != null)
            productAdapter!!.stopListening()
    }  // end of onDestroyView method

    // Override the function to add two icons on top app bar (my cart and messages)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }  // end of onCreateOptionsMenu method

    // onOptionsItemSelected events are declared here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Send to Item Cart
            R.id.tab_cart -> {
                // Create an Intent to launch CartActivity
                val intent = Intent(requireContext(), CartActivity::class.java)
                // Add product information to intent
                intent.putExtra(Constants.EXTRA_USER_INFO, mUserInfo)

                // Opens the user cart activity
                requireContext().startActivity(intent)
            }

            // Send to Messages
            R.id.tab_messages -> findNavController().navigate(R.id.home_to_messages)
        }

        return super.onOptionsItemSelected(item)
    }  // end of onOptionsItemSelected method

}  // end of HomeFragment class