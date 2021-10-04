package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //This is important to make add icon visible
        setHasOptionsMenu(true)
    } //end of onCreate method

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProductsBinding.inflate(inflater)
        return binding.root
    } //end of onCreateView method

    //Override the function to add plus icon on top app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plus_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    } //end of onCreateOptionsMenu method

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //Go to Add Products
            R.id.tab_add -> Toast.makeText(
                context, "You clicked Add Products", Toast.LENGTH_SHORT
            ).show()
        }

        return super.onOptionsItemSelected(item)
    } //end of onOptionsItemSelected method

} //end of ProductsFragment class