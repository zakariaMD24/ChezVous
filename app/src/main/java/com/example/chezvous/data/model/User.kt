package com.example.chezvous.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = UserRoles.CUSTOMER
)

object UserRoles {
    const val CUSTOMER = "CUSTOMER"
    const val PARTNER = "PARTNER"
    const val ADMIN = "ADMIN"
}
