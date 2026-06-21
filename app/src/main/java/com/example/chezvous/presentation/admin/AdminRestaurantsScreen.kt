package com.example.chezvous.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.RestaurantOrderStats
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun AdminRestaurantsScreen(
    onRestaurantClick: (restaurantId: String) -> Unit,
    onAddRestaurant: () -> Unit,
    refreshKey: Int = 0,
    viewModel: AdminRestaurantsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.refresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AdminRestaurantsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is AdminRestaurantsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ChezVousSpacing.xl),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(ChezVousSpacing.md),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    OutlinedButton(
                        onClick = viewModel::refresh,
                        modifier = Modifier.padding(top = ChezVousSpacing.md)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
                        Text("Réessayer")
                    }
                }
            }

            is AdminRestaurantsUiState.Success -> {
                if (state.restaurants.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Aucun restaurant",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ajoutez un restaurant pour commencer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm),
                        contentPadding = PaddingValues(
                            start = ChezVousSpacing.screenHorizontal,
                            end = ChezVousSpacing.screenHorizontal,
                            top = ChezVousSpacing.md,
                            bottom = 80.dp
                        )
                    ) {
                        item {
                            RestaurantsListHeader(count = state.restaurants.size)
                        }
                        items(
                            items = state.restaurants,
                            key = { it.restaurant.id }
                        ) { item ->
                            RestaurantAdminCard(
                                restaurant = item.restaurant,
                                orderStats = item.orderStats,
                                onClick = { onRestaurantClick(item.restaurant.id) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddRestaurant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(ChezVousSpacing.lg)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Ajouter un restaurant"
            )
        }
    }
}

@Composable
private fun RestaurantsListHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ChezVousSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Restaurants",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(
                    horizontal = ChezVousSpacing.sm,
                    vertical = ChezVousSpacing.xxs
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RestaurantAdminCard(
    restaurant: Restaurant,
    orderStats: RestaurantOrderStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (restaurant.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Name, cuisine, rating, status
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ChezVousSpacing.md),
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = restaurant.cuisineType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "%.1f".format(restaurant.rating),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = if (restaurant.isOpen)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (restaurant.isOpen) "Ouvert" else "Fermé",
                        modifier = Modifier.padding(
                            horizontal = ChezVousSpacing.xs,
                            vertical = ChezVousSpacing.xxs
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (restaurant.isOpen)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Order count
            Column(
                modifier = Modifier.padding(end = ChezVousSpacing.md),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${orderStats.total}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "commandes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
