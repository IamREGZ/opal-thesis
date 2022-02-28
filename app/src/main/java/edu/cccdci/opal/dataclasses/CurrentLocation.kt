package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrentLocation(
    var code: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
): Parcelable