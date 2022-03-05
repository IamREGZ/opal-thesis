package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Product information
@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val weight: Double = 0.0,
    val stock: Int = 0,
    val vendorID: String = "",
    val marketID: String = "",
    val sales: Int = 0,
    val views: Int = 0,
    val status: Int = 1
) : Parcelable

// market, sales and views variables will be used later.
