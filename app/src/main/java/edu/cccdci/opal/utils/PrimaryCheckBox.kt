package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox

class PrimaryCheckBox(
    context: Context, attrs: AttributeSet
) : AppCompatCheckBox(context, attrs), FontImplementation {

    init {
        //Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}