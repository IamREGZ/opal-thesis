package edu.cccdci.opal.utils

import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.*
import edu.cccdci.opal.ui.fragments.HomeFragment
import edu.cccdci.opal.ui.fragments.MarketsFragment
import edu.cccdci.opal.ui.fragments.OrderDetailsFragment
import java.io.IOException

class GlideLoader(
    private val context: Context,
    private val fragment: Fragment? = null
) {

    // Function to load the image using Glide
    fun loadImage(image: Any, imageView: ImageView) {
        // Set the default image, depending on the context (or fragment if any)
        val defImage = if (fragment != null) {
            // For Fragments only
            when (fragment) {
                // Product Placeholder
                is HomeFragment,
                is OrderDetailsFragment -> R.drawable.ic_product_placeholder

                // Market Placeholder
                is MarketsFragment -> R.drawable.ic_market_placeholder

                // Default Blank Image
                else -> R.drawable.ic_default_blank_image
            }
        } else {
            // For Activities only
            when (context) {
                // User Profile Image Placeholder
                is MainActivity,
                is UserProfileActivity -> R.drawable.ic_prof_image_placeholder

                // Product Placeholder
                is CartActivity,
                is CheckoutActivity,
                is ProductDescActivity,
                is ProductEditorActivity -> R.drawable.ic_product_placeholder

                // Market Placeholder
                is MarketEditorActivity,
                is MarketNavActivity,
                is MyMarketActivity -> R.drawable.ic_market_placeholder

                // Default Blank Image
                else -> R.drawable.ic_default_blank_image
            }
        }

        try {
            Glide
                // Using the current context
                .with(context)
                // Load the image URI
                .load(image)
                // How should the image be scaled
                .centerCrop()
                // Default placeholder
                .placeholder(defImage)
                // View that will load the user image
                .into(imageView)
        } catch (e: IOException) {
            // Log the error if loading the image fails
            e.printStackTrace()
        }
    }  // end of loadImage method

}  // end of GlideLoader class