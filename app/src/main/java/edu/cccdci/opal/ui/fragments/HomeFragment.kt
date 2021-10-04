package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

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
        return binding.root
    } //end of onCreateView method

    //Override the function to add two icons on top app bar (my cart and messages)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_tab_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    } //end of onCreateOptionsMenu method

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO: Send to Item Cart
            R.id.tab_cart -> Toast.makeText(
                context, "You clicked Your Cart", Toast.LENGTH_SHORT
            ).show()

            //TODO: Send to Messages
            R.id.tab_messages -> Toast.makeText(
                context, "You clicked Messages", Toast.LENGTH_SHORT
            ).show()
        }

        return super.onOptionsItemSelected(item)
    } //end of onOptionsItemSelected method

} //end of HomeFragment class