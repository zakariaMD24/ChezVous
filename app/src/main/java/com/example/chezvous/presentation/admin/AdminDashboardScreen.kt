package com.example.chezvous.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chezvous.navigation.AdminRoutes
import com.example.chezvous.presentation.auth.rememberCredentialLogoutHandler
import com.example.chezvous.ui.theme.ChezVousSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLoggedOut: () -> Unit
) {
    val viewModel: AdminDashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val notificationsViewModel: AdminNotificationsViewModel = viewModel()
    val notificationsUiState by notificationsViewModel.uiState.collectAsState()

    val adminNavController = rememberNavController()
    val backStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val onLogout = rememberCredentialLogoutHandler(onLogout = viewModel::logout)
    var restaurantsRefreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLoggedOut()
    }

    val currentSection = AdminSection.entries
        .find { it.route == currentRoute }
        ?: AdminSection.DASHBOARD

    val isOnUserDetail = currentRoute != null &&
        currentRoute.startsWith("admin/users/") &&
        currentRoute != AdminRoutes.USERS

    val isOnRestaurantDetail = currentRoute != null &&
        currentRoute.startsWith("admin/restaurants/") &&
        currentRoute != AdminRoutes.RESTAURANTS

    val isOnAddRestaurant = currentRoute == AdminRoutes.ADD_RESTAURANT
    val isOnRestaurantMenu = currentRoute == AdminRoutes.RESTAURANT_MENU
    val isOnDetailScreen = isOnUserDetail || isOnRestaurantDetail || isOnAddRestaurant || isOnRestaurantMenu

    val topBarTitle = when {
        isOnUserDetail -> "Détails utilisateur"
        isOnRestaurantDetail -> "Détails restaurant"
        isOnAddRestaurant -> "Ajouter un restaurant"
        isOnRestaurantMenu -> "Menu"
        else -> currentSection.label
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = topBarTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (isOnDetailScreen) {
                        IconButton(onClick = { adminNavController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                },
                actions = {
                    if (!isOnDetailScreen) {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Déconnexion"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            AdminBottomBar(
                currentRoute = currentRoute,
                unreadNotificationsCount = notificationsUiState.unreadCount,
                onNavigate = { section ->
                    adminNavController.navigate(section.route) {
                        popUpTo(AdminRoutes.DASHBOARD) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = adminNavController,
            startDestination = AdminRoutes.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminRoutes.DASHBOARD) {
                AdminDashboardContent(
                    uiState = uiState,
                    onRefresh = viewModel::refresh
                )
            }
            composable(AdminRoutes.RESTAURANTS) {
                AdminRestaurantsScreen(
                    onRestaurantClick = { restaurantId ->
                        adminNavController.navigate(AdminRoutes.restaurantDetail(restaurantId))
                    },
                    onAddRestaurant = {
                        adminNavController.navigate(AdminRoutes.ADD_RESTAURANT)
                    },
                    refreshKey = restaurantsRefreshKey
                )
            }
            composable(
                route = AdminRoutes.RESTAURANT_DETAIL,
                arguments = listOf(
                    navArgument(AdminRoutes.RESTAURANT_ID_ARG) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments
                    ?.getString(AdminRoutes.RESTAURANT_ID_ARG)
                    .orEmpty()
                AdminRestaurantDetailScreen(
                    onViewMenu = {
                        adminNavController.navigate(AdminRoutes.restaurantMenu(restaurantId))
                    },
                    onDeleted = {
                        restaurantsRefreshKey++
                        adminNavController.popBackStack()
                    }
                )
            }
            composable(
                route = AdminRoutes.RESTAURANT_MENU,
                arguments = listOf(
                    navArgument(AdminRoutes.RESTAURANT_ID_ARG) { type = NavType.StringType }
                )
            ) {
                AdminRestaurantMenuScreen()
            }
            composable(AdminRoutes.ADD_RESTAURANT) {
                AdminAddRestaurantScreen(
                    onSaved = {
                        restaurantsRefreshKey++
                        adminNavController.popBackStack()
                    }
                )
            }
            composable(AdminRoutes.NOTIFICATIONS) {
                AdminNotificationsScreen(
                    onNavigateToUser = { userId ->
                        adminNavController.navigate(AdminRoutes.userDetail(userId))
                    },
                    viewModel = notificationsViewModel
                )
            }
            composable(AdminRoutes.USERS) {
                AdminUsersScreen(
                    onUserClick = { userId ->
                        adminNavController.navigate(AdminRoutes.userDetail(userId))
                    }
                )
            }
            composable(
                route = AdminRoutes.USER_DETAIL,
                arguments = listOf(
                    navArgument(AdminRoutes.USER_ID_ARG) { type = NavType.StringType }
                )
            ) {
                AdminUserDetailScreen()
            }
            composable(AdminRoutes.PROFILE) {
                AdminProfileScreen()
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ChezVousSpacing.screenHorizontal)
            .padding(vertical = ChezVousSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.lg)
    ) {
        AdminWelcomeHeader(adminName = uiState.adminName)

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AdminStatsGrid(uiState = uiState)
            if (uiState.ordersByDay.isNotEmpty()) {
                OrdersChartCard(ordersByDay = uiState.ordersByDay)
            }
        }

        uiState.errorMessage?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(ChezVousSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        SystemStatusCard()

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
            Text(text = "Actualiser")
        }
    }
}

@Composable
private fun AdminSectionPlaceholder(section: AdminSection) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = section.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Section en cours de développement",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdminWelcomeHeader(adminName: String) {
    Column {
        Text(
            text = if (adminName.isNotBlank()) "Bonjour, $adminName" else "Bonjour",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tableau de bord administrateur",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdminStatsGrid(uiState: AdminDashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Group,
                title = "Utilisateurs",
                value = uiState.totalUsers.toString(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = "Commandes",
                value = uiState.totalOrders.toString(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Restaurant,
                title = "Restaurants",
                value = uiState.totalRestaurants.toString(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.DateRange,
                title = "Aujourd'hui",
                value = uiState.todayOrders.toString(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun OrdersChartCard(ordersByDay: List<Pair<String, Int>>) {
    val counts = remember(ordersByDay) { ordersByDay.map { it.second.toFloat() } }
    val maxCount = remember(counts) { counts.maxOrNull()?.coerceAtLeast(1f) ?: 1f }

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Commandes des 10 derniers jours",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "max ${maxCount.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariantColor
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val n = counts.size
                if (n < 2) return@Canvas

                val hPad = 4.dp.toPx()
                val vPadTop = 8.dp.toPx()
                val vPadBottom = 8.dp.toPx()
                val chartW = size.width - 2 * hPad
                val chartH = size.height - vPadTop - vPadBottom
                val stepX = chartW / (n - 1)

                fun xAt(i: Int) = hPad + i * stepX
                fun yAt(v: Float) = vPadTop + chartH * (1f - v / maxCount)

                val points = counts.mapIndexed { i, v -> Offset(xAt(i), yAt(v)) }

                // Dashed gridlines at 0 %, 50 %, 100 %
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = vPadTop + chartH * (1f - fraction)
                    drawLine(
                        color = gridColor.copy(alpha = 0.6f),
                        start = Offset(hPad, y),
                        end = Offset(size.width - hPad, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Filled area under the line
                val fillPath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, vPadTop + chartH)
                    lineTo(points.first().x, vPadTop + chartH)
                    close()
                }
                drawPath(fillPath, color = primaryColor.copy(alpha = 0.10f))

                // Line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = primaryColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Dots — filled ring (primary outer, surface inner)
                points.forEach { p ->
                    drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = p)
                    drawCircle(color = surfaceColor, radius = 2.dp.toPx(), center = p)
                }
            }

            // X-axis labels at D-9, D-6, D-3, today (indices 0, 3, 6, 9)
            // SpaceBetween with 4 items aligns perfectly with those 4 dot positions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(0, 3, 6, 9).forEach { idx ->
                    Text(
                        text = ordersByDay[idx].first,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariantColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Tous les systèmes sont opérationnels",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
