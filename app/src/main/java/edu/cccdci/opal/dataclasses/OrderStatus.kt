package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderStatus(
    val code: Int = 0,
    val title: String = "",
    val description: String = ""
) : Parcelable