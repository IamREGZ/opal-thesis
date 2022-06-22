package edu.cccdci.opal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater)

        with(binding) {
            // Click event for Notification Settings
            llNotification.setOnClickListener(this@SettingsFragment)
            // Click event for Location Settings
            llLocation.setOnClickListener(this@SettingsFragment)
            // Click event for Change Password
            llChangePass.setOnClickListener(this@SettingsFragment)
            // Click event for Delete Account
            llDeleteAcc.setOnClickListener(this@SettingsFragment)

            return root
        }  // end of with(binding)

    }  // end of onCreateView method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Go to Notification Settings
                R.id.ll_notification -> findNavController()
                    .navigate(R.id.settings_to_notif_settings)

                // Go to Location Settings
                R.id.ll_location -> findNavController()
                    .navigate(R.id.settings_to_loc_settings)

                // Go to Change Password
                R.id.ll_change_pass -> findNavController()
                    .navigate(R.id.settings_to_change_pass)

                // Go to Delete Account
                R.id.ll_delete_acc -> findNavController()
                    .navigate(R.id.settings_to_del_account)
            }  // end of when
        }  // end of if

    }  // end of onClick method

}  // end of SettingsFragment class
