package com.example.chezvous.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.presentation.auth.LoginScreen
import com.example.chezvous.presentation.auth.RegisterScreen
import com.example.chezvous.presentation.cart.CartScreen
import com.example.chezvous.presentation.checkout.CheckoutScreen
import com.example.chezvous.presentation.home.AllRestaurantsScreen
import com.example.chezvous.presentation.home.HomeScreen
import com.example.chezvous.presentation.orders.OrderTrackingScreen
import com.example.chezvous.presentation.orders.OrdersScreen
import com.example.chezvous.presentation.partner.PartnerDashboardScreen
import com.example.chezvous.presentation.profile.ProfileScreen
import com.example.chezvous.presentation.restaurant.RestaurantDetailsScreen
import com.example.chezvous.ui.components.ChezVousBottomBar

@Composable
fun ChezVousNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val cartItems by CartRepository.cartItems.collectAsState()
    val topLevelRoutes = setOf(
        ChezVousRoutes.HOME,
        ChezVousRoutes.RESTAURANTS,
        ChezVousRoutes.ORDERS,
        ChezVousRoutes.CART,
        ChezVousRoutes.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute in topLevelRoutes) {
                ChezVousBottomBar(
                    currentRoute = currentRoute,
                    cartItemCount = cartItems.sumOf { it.quantity },
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { rootPadding ->
        NavHost(
            navController = navController,
            startDestination = ChezVousRoutes.LOGIN,
            modifier = Modifier.padding(rootPadding)
        ) {
            composable(ChezVousRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(ChezVousRoutes.HOME) {
                            popUpTo(ChezVousRoutes.LOGIN) {
                                inclusive = true
                            }
                        }
                    },
                    onGoToRegister = {
                        navController.navigate(ChezVousRoutes.REGISTER) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(ChezVousRoutes.HOME) {
                            popUpTo(ChezVousRoutes.LOGIN) {
                                inclusive = true
                            }
                        }
                    },
                    onGoToLogin = {
                        if (!navController.popBackStack()) {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(ChezVousRoutes.HOME) {
                HomeScreen(
                    onRestaurantClick = { restaurantId ->
                        navController.navigate(ChezVousRoutes.restaurantDetails(restaurantId)) {
                            launchSingleTop = true
                        }
                    },
                    onViewAllRestaurants = {
                        navController.navigate(ChezVousRoutes.RESTAURANTS) {
                            launchSingleTop = true
                        }
                    },
                    onPartnerClick = {
                        navController.navigate(ChezVousRoutes.PARTNER_DASHBOARD) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.RESTAURANTS) {
                AllRestaurantsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onRestaurantClick = { restaurantId ->
                        navController.navigate(ChezVousRoutes.restaurantDetails(restaurantId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = ChezVousRoutes.RESTAURANT_DETAILS,
                arguments = listOf(
                    navArgument(ChezVousRoutes.RESTAURANT_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                RestaurantDetailsScreen(
                    restaurantId = backStackEntry.arguments
                        ?.getString(ChezVousRoutes.RESTAURANT_ID_ARG)
                        .orEmpty(),
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenCart = {
                        navController.navigate(ChezVousRoutes.CART) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.CART) {
                CartScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onCheckoutReady = {
                        navController.navigate(ChezVousRoutes.CHECKOUT) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.CHECKOUT) {
                CheckoutScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOrderCreated = { orderId ->
                        navController.navigate(ChezVousRoutes.orderTracking(orderId)) {
                            popUpTo(ChezVousRoutes.CART) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.ORDERS) {
                OrdersScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(ChezVousRoutes.orderTracking(orderId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = ChezVousRoutes.ORDER_TRACKING,
                arguments = listOf(
                    navArgument(ChezVousRoutes.ORDER_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                OrderTrackingScreen(
                    orderId = backStackEntry.arguments
                        ?.getString(ChezVousRoutes.ORDER_ID_ARG)
                        .orEmpty(),
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(ChezVousRoutes.PARTNER_DASHBOARD) {
                PartnerDashboardScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(ChezVousRoutes.PROFILE) {
                ProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onLoggedOut = {
                        navController.navigate(ChezVousRoutes.LOGIN) {
                            popUpTo(ChezVousRoutes.HOME) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
