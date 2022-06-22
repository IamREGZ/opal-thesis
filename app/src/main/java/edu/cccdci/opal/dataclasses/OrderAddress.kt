package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Address (Orders) information
@Parcelize
data class OrderAddress(
    val fullName: String = "",
    val phoneNum: String = "",
    val province: String = "",
    val city: String = "",
    val barangay: String = "",
    val postal: Int = 0,
    val detailAdd: String = ""
) : Parcelable
