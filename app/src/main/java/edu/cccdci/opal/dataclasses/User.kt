package edu.cccdci.opal.dataclasses

//Data class to store User information

/*
Note that gender, phoneNum, profilePic and isVendor variables
have default values since these will be user later on.
*/
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
)