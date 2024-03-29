package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton

/**
 * A class for implementing custom typeface for RadioButton
 */
class MainRadioButton(
    context: Context, attrs: AttributeSet
) : AppCompatRadioButton(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyBoldFont(context, attrs)
    }

}  // end of MainRadioButton class

