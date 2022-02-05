package edu.cccdci.opal.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.MarketEditorActivity
import edu.cccdci.opal.ui.activities.ProductEditorActivity
import java.io.IOException

class GlideLoader(private val context: Context) {

    // Function to load the image using Glide
    fun loadImage(image: Any, imageView: ImageView) {
        // Set the default image depending on the context
        val defImage = when (context) {
            is ProductEditorActivity -> R.drawable.ic_product_placeholder
            is MarketEditorActivity -> R.drawable.ic_market_placeholder
            else -> R.drawable.ic_prof_image_placeholder
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