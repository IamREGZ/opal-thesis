package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.cccdci.opal.databinding.FragmentOrderHistoryBinding

class OrderHistoryFragment : Fragment() {

    private lateinit var binding: FragmentOrderHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderHistoryBinding.inflate(inflater)
        return binding.root
    }  // end of onCreateView method

}  // end of OrderHistoryFragment class