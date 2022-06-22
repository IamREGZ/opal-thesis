package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import edu.cccdci.opal.databinding.FragmentSettingsNotifBinding

class SettingsNotifFragment : Fragment() {

    private lateinit var binding: FragmentSettingsNotifBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Inflate the layout for this fragment
        binding = FragmentSettingsNotifBinding.inflate(inflater)
        return binding.root
    }  // end of onCreateView method

}  // end of SettingsNotifFragment class
