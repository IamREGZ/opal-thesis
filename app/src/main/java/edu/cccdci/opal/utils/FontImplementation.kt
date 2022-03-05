package edu.cccdci.opal.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

/**
 * An interface containing functions to apply different font styles in texts.
 */
interface FontImplementation {
    // Function to apply regular font style in texts
    fun applyRegularFont(
        context: Context, attrs: AttributeSet
    ): Typeface = Typeface.createFromAsset(context.assets, "Ubuntu-Regular.ttf")

    // Function to apply bold font style in texts
    fun applyBoldFont(
        context: Context, attrs: AttributeSet
    ): Typeface = Typeface.createFromAsset(context.assets, "Ubuntu-Bold.ttf")

}  // end of FontImplementation interface
