package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * A class for implementing custom typeface for EditText.
 */
class MainEditText(
    context: Context, attrs: AttributeSet
) : AppCompatEditText(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}  // end of MainEditText class
