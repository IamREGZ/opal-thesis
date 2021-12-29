package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Market information
@Parcelize
data class Market(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val image: String = ""
) : Parcelable