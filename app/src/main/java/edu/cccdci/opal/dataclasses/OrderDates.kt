package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderDates(
    val orderDate: Timestamp? = null,
    val paymentDate: Timestamp? = null,
    val completeDate: Timestamp? = null,
    val returnDate: Timestamp? = null
) : Parcelable