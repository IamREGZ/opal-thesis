package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

class CustomACTV(
    context: Context, attrs: AttributeSet
) : AppCompatAutoCompleteTextView(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}