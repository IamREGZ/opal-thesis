package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Cart information
@Parcelize
data class Cart(
    val marketID: String = "",
    val cartItems: MutableList<CartItem> = mutableListOf()
) : Parcelable