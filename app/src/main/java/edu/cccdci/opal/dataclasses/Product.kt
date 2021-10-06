package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val category: String = "",
    val vendorId: String = "",
    val market: String = ""
): Parcelable