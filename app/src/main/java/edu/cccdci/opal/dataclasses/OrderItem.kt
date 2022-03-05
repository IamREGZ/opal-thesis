package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val weight: Double = 0.0,
    val qty: Int = 0,
    val totalPrice: Double = 0.0
) : Parcelable
