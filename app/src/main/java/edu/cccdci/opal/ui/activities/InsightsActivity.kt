package edu.cccdci.opal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityInsightsBinding
import edu.cccdci.opal.utils.UtilityClass

class InsightsActivity : UtilityClass() {

    private lateinit var binding: ActivityInsightsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityInsightsBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbInsightsActivity, false)

            tvInsightsSales.text = getString(
                R.string.item_price, 0.0
            )
            tvInsightsConversion.text = getString(
                R.string.percentage_value, 0.0
            )
        }  // end of with(binding)

    }  // end of onCreate method

}  // end of InsightsActivity class
