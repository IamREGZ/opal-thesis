package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox

/**
 * A class for implementing custom typeface for CheckBox (might be temporary).
 */
class MainCheckBox(
    context: Context, attrs: AttributeSet
) : AppCompatCheckBox(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}  // end of MainCheckBox class
