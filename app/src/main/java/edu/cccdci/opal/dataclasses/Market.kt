package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Market(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val profileImage: String = ""
) : Parcelable