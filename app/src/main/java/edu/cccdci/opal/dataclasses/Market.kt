package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Market information
@Parcelize
data class Market(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val wetMarket: String = "",
    val detailAdd: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val postal: Int = 0,
    val category: Int = 0,
    val otherCat: String = "",
    val vendorID: String = "",
    val deliveryFee: Double = 20.0,
    val products: Int = 0,
    val sold: Int = 0,
    val visits: Int = 0,
    val ratings: Double = 0.0
) : Parcelable