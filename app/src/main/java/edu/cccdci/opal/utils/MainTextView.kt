package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * A class for implementing custom regular typeface for TextView.
 */
class MainTextView(
    context: Context, attrs: AttributeSet
) : AppCompatTextView(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}  // end of MainTextView class
