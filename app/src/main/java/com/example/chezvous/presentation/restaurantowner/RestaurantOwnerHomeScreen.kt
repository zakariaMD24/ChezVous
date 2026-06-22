package com.example.chezvous.presentation.restaurantowner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.navigation.RestaurantOwnerRoutes
import com.example.chezvous.ui.theme.ChezVousSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantOwnerHomeScreen(
    onLoggedOut: () -> Unit = {}
) {
    val menuViewModel: RestaurantOwnerMenuViewModel = viewModel()
    val ownerNavController = rememberNavController()
    val backStackEntry by ownerNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            RestaurantOwnerBottomBar(
                currentRoute = currentRoute,
                onNavigate = { section ->
                    ownerNavController.navigate(section.route) {
                        popUpTo(RestaurantOwnerRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = ownerNavController,
            startDestination = RestaurantOwnerRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(RestaurantOwnerRoutes.HOME) {
                RestaurantOwnerHomeContent(viewModel = menuViewModel)
            }
            composable(RestaurantOwnerRoutes.MEALS) {
                RestaurantOwnerMealsScreen(viewModel = menuViewModel)
            }
            composable(RestaurantOwnerRoutes.NOTIFICATIONS) {
                RestaurantOwnerNotificationsScreen()
            }
            composable(RestaurantOwnerRoutes.PROFILE) {
                RestaurantOwnerProfileScreen(onLoggedOut = onLoggedOut)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantOwnerHomeContent(
    viewModel: RestaurantOwnerMenuViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OwnerFilterToggleBar(
            showMyRestaurantOnly = uiState.showMyRestaurantOnly,
            myRestaurantEnabled = uiState.myRestaurantId.isNotBlank(),
            onShowAll = { viewModel.setShowMyRestaurantOnly(false) },
            onShowMine = { viewModel.setShowMyRestaurantOnly(true) }
        )

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.displayedItems.isEmpty() -> {
                OwnerMenuEmptyState()
            }
            else -> {
                OwnerMenuItemList(
                    items = uiState.displayedItems,
                    restaurantNames = uiState.restaurantNames
                )
            }
        }
    }
}

// ── Shared composables used by both HOME and MEALS tabs ──────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OwnerFilterToggleBar(
    showMyRestaurantOnly: Boolean,
    myRestaurantEnabled: Boolean,
    onShowAll: () -> Unit,
    onShowMine: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ChezVousSpacing.screenHorizontal, vertical = ChezVousSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
    ) {
        FilterChip(
            selected = !showMyRestaurantOnly,
            onClick = onShowAll,
            label = { Text("Tous les plats") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = showMyRestaurantOnly,
            onClick = onShowMine,
            label = { Text("Mon restaurant") },
            enabled = myRestaurantEnabled,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider()
}

@Composable
internal fun OwnerMenuItemList(
    items: List<FoodItem>,
    restaurantNames: Map<String, String>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = ChezVousSpacing.screenHorizontal,
            vertical = ChezVousSpacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
    ) {
        items(items, key = { it.id }) { item ->
            OwnerFoodItemCard(
                item = item,
                restaurantName = restaurantNames[item.restaurantId].orEmpty()
            )
        }
    }
}

@Composable
internal fun OwnerFoodItemCard(
    item: FoodItem,
    restaurantName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.category.isNotBlank()) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (restaurantName.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = restaurantName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = item.price.ownerPriceText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun OwnerMenuEmptyState(
    message: String = "Aucun plat n'est disponible pour le moment."
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(ChezVousSpacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Aucun plat trouvé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

internal fun Double.ownerPriceText(): String =
    if (this % 1.0 == 0.0) "${toInt()} DH" else "$this DH"
