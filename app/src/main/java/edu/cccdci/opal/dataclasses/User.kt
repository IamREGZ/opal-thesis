package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Data class to store User information
@Parcelize
data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val emailAdd: String = "",
    val userName: String = "",
    val gender: String = "",
    val phoneNum: String = "",
    val profilePic: String = "",
    val isVendor: Boolean = false
) : Parcelable

/*
gender, phoneNum, profilePic and isVendor variables
will be used later on.
*/