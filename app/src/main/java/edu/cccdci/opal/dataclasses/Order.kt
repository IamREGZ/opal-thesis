package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store Order information
@Parcelize
data class Order(
    val id: String = "",
    val dates: OrderDates? = null,  // Dynamic
    val status: OrderStatus? = null,  // Dynamic
    val payment: String = "",
    val customerID: String = "",
    val custUser: String = "",
    val address: OrderAddress? = null,
    val marketID: String = "",
    val marketName: String = "",
    val vendorID: String = "",
    val orderItems: List<OrderItem> = listOf(),  // Dynamic
    val subtotal: Double = 0.0,  // Dynamic
    val deliveryFee: Double = 0.0,
    val totalPrice: Double = 0.0,  // Dynamic
    val orderAction: String = "",
    val special: String = ""
) : Parcelable