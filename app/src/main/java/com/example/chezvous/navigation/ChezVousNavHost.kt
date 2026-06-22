package com.example.chezvous.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.UserRepository
import com.example.chezvous.presentation.auth.LoginScreen
import com.example.chezvous.presentation.auth.RegisterScreen
import com.example.chezvous.presentation.cart.CartScreen
import com.example.chezvous.presentation.checkout.CheckoutScreen
import com.example.chezvous.presentation.delivery.DeliveryDashboardScreen
import com.example.chezvous.presentation.home.AllRestaurantsScreen
import com.example.chezvous.presentation.home.HomeScreen
import com.example.chezvous.presentation.orders.OrderTrackingScreen
import com.example.chezvous.presentation.orders.OrdersScreen
import com.example.chezvous.presentation.partner.PartnerDashboardScreen
import com.example.chezvous.presentation.profile.ProfileScreen
import com.example.chezvous.presentation.restaurant.RestaurantDetailsScreen
import com.example.chezvous.ui.components.ChezVousBottomBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChezVousNavHost() {
    val navController = rememberNavController()
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val userRepository = remember { UserRepository() }
    var currentUserId by remember { mutableStateOf(firebaseAuth.currentUser?.uid.orEmpty()) }
    var currentRole by remember { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val cartItems by CartRepository.cartItems.collectAsState()
    val customerTopLevelRoutes = setOf(
        ChezVousRoutes.HOME,
        ChezVousRoutes.RESTAURANTS,
        ChezVousRoutes.ORDERS,
        ChezVousRoutes.CART,
        ChezVousRoutes.PROFILE
    )
    val managementTopLevelRoutes = setOf(
        ChezVousRoutes.PARTNER_DASHBOARD,
        ChezVousRoutes.PROFILE
    )
    val driverTopLevelRoutes = setOf(
        ChezVousRoutes.DELIVERY_DASHBOARD,
        ChezVousRoutes.PROFILE
    )
    val currentRoleForUi = currentRole ?: UserRoles.CUSTOMER
    val roleHomeRoute = if (currentUserId.isNotBlank() && currentRole == null) {
        ChezVousRoutes.ROLE_LANDING
    } else {
        roleHomeRouteFor(currentRole)
    }
    val bottomBarRoutes = if (currentUserId.isNotBlank() && currentRole == null) {
        emptySet()
    } else {
        when {
            UserRoles.canUsePartnerDashboard(currentRoleForUi) -> managementTopLevelRoutes
            UserRoles.canUseDriverDashboard(currentRoleForUi) -> driverTopLevelRoutes
            else -> customerTopLevelRoutes
        }
    }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUserId = auth.currentUser?.uid.orEmpty()
            if (auth.currentUser == null) {
                currentRole = null
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUserId) {
        currentRole = null

        if (currentUserId.isBlank()) {
            return@LaunchedEffect
        }

        userRepository.observeUser(currentUserId).collect { user ->
            currentRole = UserRoles.safeRole(user?.role)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute in bottomBarRoutes) {
                ChezVousBottomBar(
                    currentRoute = currentRoute,
                    cartItemCount = cartItems.sumOf { it.quantity },
                    role = currentRoleForUi,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(roleHomeRoute) {
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
            startDestination = if (currentUserId.isBlank()) {
                ChezVousRoutes.LOGIN
            } else {
                ChezVousRoutes.ROLE_LANDING
            },
            modifier = Modifier.padding(rootPadding)
        ) {
            composable(ChezVousRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        currentUserId = firebaseAuth.currentUser?.uid.orEmpty()
                        navController.navigate(ChezVousRoutes.ROLE_LANDING) {
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
                        currentUserId = firebaseAuth.currentUser?.uid.orEmpty()
                        navController.navigate(ChezVousRoutes.ROLE_LANDING) {
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

            composable(ChezVousRoutes.ROLE_LANDING) {
                RoleLandingScreen(
                    currentUserId = currentUserId,
                    currentRole = currentRole,
                    onLoggedOut = {
                        navController.navigate(ChezVousRoutes.LOGIN) {
                            popUpTo(ChezVousRoutes.ROLE_LANDING) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onRoleResolved = { route ->
                        navController.navigate(route) {
                            popUpTo(ChezVousRoutes.ROLE_LANDING) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ChezVousRoutes.HOME) {
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.HOME) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
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
            }

            composable(ChezVousRoutes.RESTAURANTS) {
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.RESTAURANTS) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    AllRestaurantsScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        showBackButton = false,
                        onRestaurantClick = { restaurantId ->
                            navController.navigate(ChezVousRoutes.restaurantDetails(restaurantId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(
                route = ChezVousRoutes.RESTAURANT_DETAILS,
                arguments = listOf(
                    navArgument(ChezVousRoutes.RESTAURANT_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.RESTAURANT_DETAILS) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
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
            }

            composable(ChezVousRoutes.CART) {
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.CART) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    CartScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        showBackButton = false,
                        onCheckoutReady = {
                            navController.navigate(ChezVousRoutes.CHECKOUT) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(ChezVousRoutes.CHECKOUT) {
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.CHECKOUT) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
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
            }

            composable(ChezVousRoutes.ORDERS) {
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.ORDERS) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    OrdersScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        showBackButton = false,
                        onOrderClick = { orderId ->
                            navController.navigate(ChezVousRoutes.orderTracking(orderId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(
                route = ChezVousRoutes.ORDER_TRACKING,
                arguments = listOf(
                    navArgument(ChezVousRoutes.ORDER_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                if (!canUseCustomerArea(currentUserId, currentRole)) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.ORDER_TRACKING) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    OrderTrackingScreen(
                        orderId = backStackEntry.arguments
                            ?.getString(ChezVousRoutes.ORDER_ID_ARG)
                            .orEmpty(),
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(ChezVousRoutes.PARTNER_DASHBOARD) {
                if (currentUserId.isBlank() || currentRole == null ||
                    !UserRoles.canUsePartnerDashboard(currentRole)
                ) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.PARTNER_DASHBOARD) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    PartnerDashboardScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        showBackButton = false
                    )
                }
            }

            composable(ChezVousRoutes.DELIVERY_DASHBOARD) {
                if (currentUserId.isBlank() || currentRole == null ||
                    !UserRoles.canUseDriverDashboard(currentRole)
                ) {
                    RoleLandingScreen(
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        onLoggedOut = {
                            navController.navigate(ChezVousRoutes.LOGIN) {
                                launchSingleTop = true
                            }
                        },
                        onRoleResolved = { route ->
                            navController.navigate(route) {
                                popUpTo(ChezVousRoutes.DELIVERY_DASHBOARD) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    DeliveryDashboardScreen(
                        onBack = {
                            navController.popBackStack()
                        },
                        showBackButton = false
                    )
                }
            }

            composable(ChezVousRoutes.PROFILE) {
                ProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    showBackButton = false,
                    onLoggedOut = {
                        navController.navigate(ChezVousRoutes.LOGIN) {
                            popUpTo(roleHomeRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RoleLandingScreen(
    currentUserId: String,
    currentRole: String?,
    onLoggedOut: () -> Unit,
    onRoleResolved: (String) -> Unit
) {
    LaunchedEffect(currentUserId, currentRole) {
        if (currentUserId.isBlank()) {
            onLoggedOut()
            return@LaunchedEffect
        }

        currentRole?.let { role ->
            onRoleResolved(roleHomeRouteFor(role))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun roleHomeRouteFor(role: String?): String {
    return when {
        UserRoles.canUsePartnerDashboard(role) -> ChezVousRoutes.PARTNER_DASHBOARD
        UserRoles.canUseDriverDashboard(role) -> ChezVousRoutes.DELIVERY_DASHBOARD
        else -> ChezVousRoutes.HOME
    }
}

private fun canUseCustomerArea(
    currentUserId: String,
    currentRole: String?
): Boolean {
    return currentUserId.isBlank() ||
            (currentRole != null && UserRoles.canOrderAsCustomer(currentRole))
}
