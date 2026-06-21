package com.example.chezvous.presentation.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.repository.RestaurantOrderStats
import com.example.chezvous.data.repository.RestaurantOrderSummary
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminRestaurantDetailScreen(
    onViewMenu: () -> Unit = {},
    onDeleted: () -> Unit = {},
    viewModel: AdminRestaurantDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    uiState.errorMessage?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ChezVousSpacing.xl),
            contentAlignment = Alignment.Center
        ) {
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
        return
    }

    val restaurant = uiState.restaurant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.lg)
    ) {
        // Full-width image header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!restaurant?.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = restaurant!!.imageUrl,
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
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .chezVousScreenPadding()
                .padding(bottom = ChezVousSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.lg)
        ) {
            DeleteRestaurantButton(
                isDeleting = uiState.isDeleting,
                onClick = { showDeleteDialog = true }
            )

            uiState.deleteErrorMessage?.let { err ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = err,
                        modifier = Modifier.padding(ChezVousSpacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            RestaurantInfoHeader(
                name = restaurant?.name ?: "—",
                cuisineType = restaurant?.cuisineType ?: "—",
                rating = restaurant?.rating ?: 0.0,
                deliveryTime = restaurant?.deliveryTime ?: "—",
                isOpen = restaurant?.isOpen ?: false
            )

            RestaurantStatsGrid(stats = uiState.orderStats)

            RestaurantPropertiesSection(
                minimumOrder = restaurant?.minimumOrder ?: 0.0,
                deliveryTime = restaurant?.deliveryTime ?: "—",
                isOpen = restaurant?.isOpen ?: false
            )

            ViewMenuCard(onClick = onViewMenu)

            RestaurantRecentOrdersSection(orders = uiState.recentOrders)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isDeleting) showDeleteDialog = false },
            title = { Text("Supprimer le restaurant") },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer ce restaurant ? Cette action est irréversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRestaurant()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun DeleteRestaurantButton(isDeleting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isDeleting,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        if (isDeleting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onError
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
        Text(
            text = if (isDeleting) "Suppression..." else "Supprimer ce restaurant",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun RestaurantInfoHeader(
    name: String,
    cuisineType: String,
    rating: Double,
    deliveryTime: String,
    isOpen: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = if (isOpen)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (isOpen) "Ouvert" else "Fermé",
                    modifier = Modifier.padding(
                        horizontal = ChezVousSpacing.sm,
                        vertical = ChezVousSpacing.xxs
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOpen)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = cuisineType,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "·",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = deliveryTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RestaurantStatsGrid(stats: RestaurantOrderStats) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Statistiques",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            RestaurantStatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = "${stats.total}",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            RestaurantStatCard(
                modifier = Modifier.weight(1f),
                label = "Actives",
                value = "${stats.active}",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            RestaurantStatCard(
                modifier = Modifier.weight(1f),
                label = "Complétées",
                value = "${stats.completed}",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            RestaurantStatCard(
                modifier = Modifier.weight(1f),
                label = "Revenus",
                value = "%.0f DA".format(stats.revenue),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RestaurantStatCard(
    modifier: Modifier = Modifier,
    label: String,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun RestaurantPropertiesSection(
    minimumOrder: Double,
    deliveryTime: String,
    isOpen: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Informations",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        RestaurantInfoRow(
            icon = { Icon(Icons.Outlined.AttachMoney, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
            label = "Commande minimum",
            value = "%.0f DA".format(minimumOrder)
        )
        RestaurantInfoRow(
            icon = { Icon(Icons.Outlined.AccessTime, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
            label = "Délai de livraison",
            value = deliveryTime
        )
        RestaurantInfoRow(
            icon = { Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp), tint = if (isOpen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant) },
            label = "Statut",
            value = if (isOpen) "Ouvert" else "Fermé"
        )
    }
}

@Composable
private fun RestaurantInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            icon()
            Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RestaurantRecentOrdersSection(orders: List<RestaurantOrderSummary>) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Commandes récentes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (orders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(ChezVousSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Aucune commande pour ce restaurant.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            orders.forEach { order ->
                RestaurantOrderCard(order = order)
            }
        }

        Spacer(modifier = Modifier.height(ChezVousSpacing.md))
    }
}

@Composable
private fun RestaurantOrderCard(order: RestaurantOrderSummary) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = remember(order.createdAt) {
        if (order.createdAt > 0L) dateFormatter.format(Date(order.createdAt)) else "—"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (order.itemCount > 0) "${order.itemCount} article${if (order.itemCount > 1) "s" else ""}" else "Commande",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.0f DA".format(order.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                RestaurantOrderStatusBadge(status = order.status)
            }
        }
    }
}

@Composable
private fun ViewMenuCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Menu du restaurant",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Voir tous les plats disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RestaurantOrderStatusBadge(status: OrderStatus) {
    val (label, containerColor, contentColor) = when (status) {
        OrderStatus.DELIVERED -> Triple("Livrée", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        OrderStatus.CANCELLED -> Triple("Annulée", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        OrderStatus.ON_THE_WAY -> Triple("En livraison", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        OrderStatus.PREPARING -> Triple("En préparation", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        OrderStatus.CONFIRMED -> Triple("Confirmée", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        OrderStatus.PENDING -> Triple("En attente", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = ChezVousSpacing.xs, vertical = ChezVousSpacing.xxs),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
