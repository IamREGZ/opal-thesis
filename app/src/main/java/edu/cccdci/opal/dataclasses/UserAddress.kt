package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Address information
@Parcelize
data class UserAddress(
    val addressID: String = "",
    val fullName: String = "",
    val phoneNum: String = "",
    val province: String = "",
    val city: String = "",
    val barangay: String = "",
    val postal: Int = 0,
    val detailAdd: String = "",
    val default: Boolean = false,
    val location: LocationData? = null
) : Parcelable
