package com.example.chezvous.navigation

import android.net.Uri

object ChezVousRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val RESTAURANT_ID_ARG = "restaurantId"
    const val RESTAURANT_DETAILS = "restaurant/{$RESTAURANT_ID_ARG}"

    fun restaurantDetails(restaurantId: String): String {
        return "restaurant/${Uri.encode(restaurantId)}"
    }
}
