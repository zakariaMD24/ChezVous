package com.example.chezvous.presentation.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chezvous.navigation.AdminRoutes

enum class AdminSection(val label: String, val icon: ImageVector, val route: String) {
    DASHBOARD("Dashboard", Icons.Outlined.Home, AdminRoutes.DASHBOARD),
    RESTAURANTS("Restaurants", Icons.Outlined.Restaurant, AdminRoutes.RESTAURANTS),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications, AdminRoutes.NOTIFICATIONS),
    USERS("Utilisateurs", Icons.Outlined.Group, AdminRoutes.USERS),
    PROFILE("Profil", Icons.Outlined.Person, AdminRoutes.PROFILE)
}
