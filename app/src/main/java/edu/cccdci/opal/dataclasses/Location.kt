package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val lat: Double = 0.0,
    val lng: Double = 0.0
) : GeoPoint(lat, lng), Parcelable