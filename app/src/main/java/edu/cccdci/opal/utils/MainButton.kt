package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

/**
 * A class for implementing custom typeface for Button.
 */
class MainButton(
    context: Context, attrs: AttributeSet
) : MaterialButton(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyBoldFont(context, attrs)
    }

}  // end of MainButton class
