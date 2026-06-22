package com.example.chezvous.presentation.restaurantowner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chezvous.navigation.RestaurantOwnerRoutes

enum class RestaurantOwnerSection(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Accueil", Icons.Outlined.Home, RestaurantOwnerRoutes.HOME),
    MEALS("Mes Plats", Icons.Outlined.Restaurant, RestaurantOwnerRoutes.MEALS),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications, RestaurantOwnerRoutes.NOTIFICATIONS),
    PROFILE("Profil", Icons.Outlined.Person, RestaurantOwnerRoutes.PROFILE)
}
