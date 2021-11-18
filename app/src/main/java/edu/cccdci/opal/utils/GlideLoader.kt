package edu.cccdci.opal.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import edu.cccdci.opal.R
import java.io.IOException

class GlideLoader(val context: Context) {

    // Function to load the user image using Glide
    fun loadUserPicture(image: Any, imageView: ImageView) {
        try {
            Glide
                .with(context)  // Using the current context
                .load(image)  // Load the image URI
                .centerCrop()  // How should the image be scaled
                .placeholder(R.drawable.ic_prof_image_placeholder)  // Default placeholder
                .into(imageView)  // View that will load the user image
        } catch (e: IOException) {
            // Log the error if loading the image fails
            e.printStackTrace()
        }
    }  // end of loadUserPicture method

}  // end of GlideLoader class