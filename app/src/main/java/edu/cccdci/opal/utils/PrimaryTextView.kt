package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class PrimaryTextView(
    context: Context, attrs: AttributeSet
) : AppCompatTextView(context, attrs), FontImplementation {

    init {
        typeface = applyRegularFont(context, attrs) //Initiates the custom font change
    }

}