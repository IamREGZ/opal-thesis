package edu.cccdci.opal.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class to store User information
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
    val marketID: String = "",
    val locSettings: CurrentLocation? = null,
    val cart: Cart? = null
) : Parcelable

/* gender, phoneNum, profilePic, vendor, marketID, locSettings, and cart
 * variables will be used later upon account registration.
 */
