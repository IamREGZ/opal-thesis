package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    val vendor: Boolean = false,
    val cartItems: MutableList<Cart> = mutableListOf()
) : Parcelable

/* gender, phoneNum, profilePic and vendor variables
 * will be used later on upon account registration.
 */