package edu.cccdci.opal.utils

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.switchmaterial.SwitchMaterial

class MainSwitchToggle(
    context: Context, attrs: AttributeSet
): SwitchMaterial(context, attrs), FontImplementation {

    init {
        //Initiates the custom font change
        typeface = applyRegularFont(context, attrs)
    }

}