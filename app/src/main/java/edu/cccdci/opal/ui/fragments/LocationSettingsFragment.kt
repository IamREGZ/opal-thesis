package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.cccdci.opal.databinding.FragmentLocationSettingsBinding

class LocationSettingsFragment : Fragment() {

    private lateinit var binding: FragmentLocationSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLocationSettingsBinding.inflate(inflater)
        return binding.root
    }  // end of onCreateView method

}  // end of LocationSettingsFragment class