package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Market information
@Parcelize
data class Market(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val detailAdd: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val vendorID: String = "",
    val deliveryFee: Double = 0.0
) : Parcelable