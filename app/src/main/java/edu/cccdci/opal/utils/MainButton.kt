package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class MainButton(
    context: Context, attrs: AttributeSet
) : AppCompatButton(context, attrs), FontImplementation {

    init {
        //Initiates the custom font change
        typeface = applyBoldFont(context, attrs)
    }

}