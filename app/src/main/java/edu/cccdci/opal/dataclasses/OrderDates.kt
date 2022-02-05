package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderDates(
    val orderDate: Timestamp? = null,
    var paymentDate: Timestamp? = null,
    val deliverDate: Timestamp? = null,
    val returnDate: Timestamp? = null
) : Parcelable