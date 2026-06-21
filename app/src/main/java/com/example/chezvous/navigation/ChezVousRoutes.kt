package com.example.chezvous.navigation

import android.net.Uri

object ChezVousRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val RESTAURANTS = "restaurants"
    const val PARTNER_DASHBOARD = "partner-dashboard"
    const val ADMIN_DASHBOARD = "admin-dashboard"
    const val RESTAURANT_ID_ARG = "restaurantId"
    const val ORDER_ID_ARG = "orderId"
    const val RESTAURANT_DETAILS = "restaurant/{$RESTAURANT_ID_ARG}"
    const val ORDER_TRACKING = "orders/{$ORDER_ID_ARG}"

    fun restaurantDetails(restaurantId: String): String {
        return "restaurant/${Uri.encode(restaurantId)}"
    }

    fun orderTracking(orderId: String): String {
        return "orders/${Uri.encode(orderId)}"
    }
}
