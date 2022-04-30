package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrentLocation(
    val code: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val fullAddress: String = ""
) : Parcelable
