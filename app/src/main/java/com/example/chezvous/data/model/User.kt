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
    const val RESTAURANT_ADMIN = "RESTAURANT_ADMIN"
    const val CHEF = "CHEF"
    const val DRIVER = "DRIVER"
    const val ADMIN = "ADMIN"

    fun canUsePartnerDashboard(role: String?): Boolean {
        return role == PARTNER || role == RESTAURANT_ADMIN || role == ADMIN
    }

    fun canUseDriverDashboard(role: String?): Boolean {
        return role == DRIVER
    }

    fun canUseKitchenDashboard(role: String?): Boolean {
        return role == CHEF
    }

    fun canOrderAsCustomer(role: String?): Boolean {
        return role == null || role == CUSTOMER
    }

    fun hasGlobalRestaurantAccess(role: String?): Boolean {
        return role == ADMIN
    }
}
