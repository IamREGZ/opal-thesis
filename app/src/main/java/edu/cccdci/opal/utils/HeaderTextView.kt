package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class HeaderTextView(
    context: Context, attrs: AttributeSet
) : AppCompatTextView(context, attrs), FontImplementation {

    init {
        // Initiates the custom font change
        typeface = applyBoldFont(context, attrs)
    }

}