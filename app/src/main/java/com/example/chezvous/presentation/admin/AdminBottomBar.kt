package com.example.chezvous.presentation.admin

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    unreadNotificationsCount: Int = 0,
    onNavigate: (AdminSection) -> Unit
) {
    NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
        AdminSection.entries.forEach { section ->
            val showBadge = section == AdminSection.NOTIFICATIONS && unreadNotificationsCount > 0
            NavigationBarItem(
                selected = currentRoute == section.route,
                onClick = { onNavigate(section) },
                icon = {
                    if (showBadge) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = if (unreadNotificationsCount > 9) "9+"
                                               else unreadNotificationsCount.toString()
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = section.label
                        )
                    }
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
