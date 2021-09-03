package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class PrimaryButton(
    context: Context, attrs: AttributeSet
) : AppCompatButton(context, attrs), FontImplementation {

    init {
        typeface = applyRegularFont(context, attrs) //Initiates the custom font change
    }

}