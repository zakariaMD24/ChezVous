package com.example.chezvous.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = UserRoles.CUSTOMER,
    val managedRestaurantIds: List<String> = emptyList(),
    val driverId: String = ""
)

object UserRoles {
    const val CUSTOMER = "CUSTOMER"
    const val PARTNER = "PARTNER"
    const val DRIVER = "DRIVER"
    const val ADMIN = "ADMIN"

    fun safeRole(role: String?): String {
        return when (role) {
            ADMIN -> ADMIN
            PARTNER, "RESTAURANT_ADMIN", "CHEF" -> PARTNER
            DRIVER -> DRIVER
            else -> CUSTOMER
        }
    }

    fun canUsePartnerDashboard(role: String?): Boolean {
        val safe = safeRole(role)
        return safe == PARTNER || safe == ADMIN
    }

    fun canUseDriverDashboard(role: String?): Boolean {
        return safeRole(role) == DRIVER
    }

    fun canOrderAsCustomer(role: String?): Boolean {
        return safeRole(role) == CUSTOMER
    }

    fun hasGlobalRestaurantAccess(role: String?): Boolean {
        return safeRole(role) == ADMIN
    }
}
