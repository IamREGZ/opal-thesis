package edu.cccdci.opal.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

interface FontImplementation {
    //Function to apply regular font style in texts
    fun applyRegularFont(context: Context, attrs: AttributeSet): Typeface =
        Typeface.createFromAsset(context.assets, "Ubuntu-Regular.ttf")

    //Function to apply bold font style in texts
    fun applyBoldFont(context: Context, attrs: AttributeSet): Typeface =
        Typeface.createFromAsset(context.assets, "Ubuntu-Bold.ttf")
}