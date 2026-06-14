package com.example.chezvous.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chezvous.presentation.auth.LoginScreen
import com.example.chezvous.presentation.auth.RegisterScreen
import com.example.chezvous.presentation.cart.CartScreen
import com.example.chezvous.presentation.home.HomeScreen
import com.example.chezvous.presentation.profile.ProfileScreen
import com.example.chezvous.presentation.restaurant.RestaurantDetailsScreen

@Composable
fun ChezVousNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ChezVousRoutes.LOGIN
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
                onProfileClick = {
                    navController.navigate(ChezVousRoutes.PROFILE) {
                        launchSingleTop = true
                    }
                },
                onCartClick = {
                    navController.navigate(ChezVousRoutes.CART) {
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
