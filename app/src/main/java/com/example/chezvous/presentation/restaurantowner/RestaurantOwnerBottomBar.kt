package com.example.chezvous.presentation.restaurantowner

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun RestaurantOwnerBottomBar(
    currentRoute: String?,
    onNavigate: (RestaurantOwnerSection) -> Unit
) {
    NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
        RestaurantOwnerSection.entries.forEach { section ->
            NavigationBarItem(
                selected = currentRoute == section.route,
                onClick = { onNavigate(section) },
                icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.label
                    )
                },
                label = {
                    Text(
                        text = section.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
