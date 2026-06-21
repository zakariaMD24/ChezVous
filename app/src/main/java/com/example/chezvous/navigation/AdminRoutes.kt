package com.example.chezvous.navigation

import android.net.Uri

object AdminRoutes {
    const val DASHBOARD = "admin/dashboard"
    const val RESTAURANTS = "admin/restaurants"
    const val ORDERS = "admin/orders"
    const val NOTIFICATIONS = "admin/notifications"
    const val USERS = "admin/users"
    const val PROFILE = "admin/profile"

    const val USER_ID_ARG = "userId"
    const val USER_DETAIL = "admin/users/{$USER_ID_ARG}"

    const val RESTAURANT_ID_ARG = "restaurantId"
    const val RESTAURANT_DETAIL = "admin/restaurants/{$RESTAURANT_ID_ARG}"
    const val RESTAURANT_MENU = "admin/restaurants/{$RESTAURANT_ID_ARG}/menu"
    const val ADD_RESTAURANT = "admin/add-restaurant"

    fun userDetail(userId: String): String = "admin/users/${Uri.encode(userId)}"
    fun restaurantDetail(restaurantId: String): String = "admin/restaurants/${Uri.encode(restaurantId)}"
    fun restaurantMenu(restaurantId: String): String = "admin/restaurants/${Uri.encode(restaurantId)}/menu"
}
