package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class PrimaryEditText(
    context: Context, attrs: AttributeSet
) : AppCompatEditText(context, attrs), FontImplementation {

    init {
        //Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}