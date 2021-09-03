package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class PrimaryEditText(
    context: Context, attrs: AttributeSet
) : AppCompatEditText(context, attrs), FontImplementation {

    init {
        typeface = applyRegularFont(context, attrs) //Initiates the custom font change
    }

}