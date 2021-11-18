package edu.cccdci.opal.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

interface FontImplementation {
    // Function to apply regular font style in texts
    fun applyRegularFont(context: Context, attrs: AttributeSet): Typeface {
        return Typeface.createFromAsset(context.assets, "Ubuntu-Regular.ttf")
    }  // end of applyRegularFont method

    // Function to apply bold font style in texts
    fun applyBoldFont(context: Context, attrs: AttributeSet): Typeface {
        return Typeface.createFromAsset(context.assets, "Ubuntu-Bold.ttf")
    }  // end of applyBoldFont method

}  // end of FontImplementation interface