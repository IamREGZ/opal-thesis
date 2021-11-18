package edu.cccdci.opal.ui.activities

import android.os.Bundle
import android.view.View
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityProductEditorBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.utils.Constants

class ProductEditorActivity : TemplateActivity() {

    private lateinit var binding: ActivityProductEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductEditorBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // val prodInfo: Product

            // Setups the Action Bar of the current activity
            setupActionBar(tlbProdEditActivity, false)

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.PRODUCT_DESCRIPTION)) {
                // Store the information in the prodInfo object (might be temporary)
                val prodInfo: Product = intent
                    .getParcelableExtra(Constants.PRODUCT_DESCRIPTION)!!

                // Change the header of the Toolbar
                tvProdEditTitle.text = resources.getString(R.string.tlb_title_edit_prod)
                // Make the delete button visible
                btnDeleteProduct.visibility = View.VISIBLE

                // Sets the values to the EditTexts
                with(prodInfo) {
                    etProdEditName.setText(name)
                    etProdEditPrice.setText(price.toString())
                }
            }  // end of if

        }  // end of with(binding)

    }  // end of onCreate method

}  // end of ProductEditorActivity class