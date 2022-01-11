package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Cart Item information
@Parcelize
data class CartItem(
    val prodID: String = "",
    var prodQTY: Int = 1,
    var prodPrice: Double = 0.0
) : Parcelable