package edu.cccdci.opal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.ProductAdapter
import edu.cccdci.opal.databinding.FragmentHomeBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.ui.activities.CartActivity
import edu.cccdci.opal.utils.Constants

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //This is important to make two icons (my cart and messages) visible
        setHasOptionsMenu(true)
    } //end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        val dataList = mutableListOf<Product>()

        var i = 1
        while (i <= 10) {
            dataList.add(
                Product(
                    name = "Product $i",
                    price = i.toDouble(),
                    market = "Market $i"
                )
            )
            i++
        }

        productAdapter = ProductAdapter(requireContext(), dataList)

        with(binding) {
            rvHome.adapter = productAdapter
            rvHome.layoutManager = GridLayoutManager(context, 2)

            if (productAdapter.itemCount == 0) {
                rvHome.visibility = View.GONE
                llEmptyHome.visibility = View.VISIBLE
            } else {
                rvHome.visibility = View.VISIBLE
                llEmptyHome.visibility = View.GONE
            }

            return root
        }
    } //end of onCreateView method

    //Override the function to add two icons on top app bar (my cart and messages)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    } //end of onCreateOptionsMenu method

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Send to Item Cart
            R.id.tab_cart -> {
                val user = User()

                val intent = Intent(requireContext(), CartActivity::class.java)
                //Add product information to intent
                intent.putExtra(Constants.EXTRA_USER_INFO, user)

                requireContext().startActivity(intent)
            }

            //Send to Messages
            R.id.tab_messages -> findNavController().navigate(R.id.home_to_messages)
        }

        return super.onOptionsItemSelected(item)
    } //end of onOptionsItemSelected method

} //end of HomeFragment class