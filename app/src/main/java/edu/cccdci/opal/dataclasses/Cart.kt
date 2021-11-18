package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val price: Double = 0.0,
    val qty: Int = 0,
    val vendor: String = "",
    val market: String = "",
    var isSelected: Boolean = false
) : Parcelable