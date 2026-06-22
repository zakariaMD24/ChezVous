package com.example.chezvous.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.chezvous.R
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.navigation.ChezVousRoutes

class ChezVousBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun ChezVousBottomBar(
    currentRoute: String?,
    cartItemCount: Int,
    role: String = UserRoles.CUSTOMER,
    onNavigate: (String) -> Unit
) {
    val items = if (UserRoles.canUsePartnerDashboard(role)) {
        listOf(
            ChezVousBottomBarItem(
                route = ChezVousRoutes.PARTNER_DASHBOARD,
                label = if (UserRoles.hasGlobalRestaurantAccess(role)) {
                    stringResource(R.string.admin_space)
                } else {
                    stringResource(R.string.partner_space)
                },
                icon = Icons.Outlined.Restaurant
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.PROFILE,
                label = stringResource(R.string.nav_profile),
                icon = Icons.Outlined.Person
            )
        )
    } else if (UserRoles.canUseDriverDashboard(role)) {
        listOf(
            ChezVousBottomBarItem(
                route = ChezVousRoutes.DELIVERY_DASHBOARD,
                label = stringResource(R.string.nav_delivery),
                icon = Icons.Outlined.DeliveryDining
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.PROFILE,
                label = stringResource(R.string.nav_profile),
                icon = Icons.Outlined.Person
            )
        )
    } else {
        listOf(
            ChezVousBottomBarItem(
                route = ChezVousRoutes.HOME,
                label = stringResource(R.string.nav_home),
                icon = Icons.Outlined.Home
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.RESTAURANTS,
                label = stringResource(R.string.nav_explore),
                icon = Icons.Outlined.Restaurant
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.ORDERS,
                label = stringResource(R.string.nav_orders),
                icon = Icons.Outlined.ReceiptLong
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.CART,
                label = stringResource(R.string.nav_cart),
                icon = Icons.Outlined.ShoppingCart
            ),
            ChezVousBottomBarItem(
                route = ChezVousRoutes.PROFILE,
                label = stringResource(R.string.nav_profile),
                icon = Icons.Outlined.Person
            )
        )
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    if (item.route == ChezVousRoutes.CART && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(cartItemCount.toString())
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}
