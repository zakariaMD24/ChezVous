package com.example.chezvous.presentation.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.OrderStatusChip
import com.example.chezvous.ui.components.PickupCodeCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.customerLabel
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

@Composable
fun KitchenDashboardScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    val viewModel: KitchenDashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.message, uiState.errorMessage) {
        if (uiState.message != null || uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Espace cuisine",
                onBack = if (showBackButton) onBack else null
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                KitchenLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            !uiState.isAuthorized -> {
                KitchenMessageState(
                    title = "Acces cuisine requis",
                    message = uiState.errorMessage.orEmpty(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            uiState.selectedRestaurant == null -> {
                KitchenRestaurantSelection(
                    restaurants = uiState.restaurants,
                    message = uiState.message,
                    errorMessage = uiState.errorMessage,
                    onRestaurantSelected = viewModel::selectRestaurant,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                KitchenOrdersContent(
                    uiState = uiState,
                    onChangeRestaurant = viewModel::clearRestaurantSelection,
                    onUpdateOrderStatus = viewModel::updateOrderStatus,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun KitchenRestaurantSelection(
    restaurants: List<Restaurant>,
    message: String?,
    errorMessage: String?,
    onRestaurantSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Text(
                text = "Choisir la cuisine",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Selectionnez le restaurant avant de preparer les commandes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            KitchenMessageArea(message = message, errorMessage = errorMessage)
        }

        if (restaurants.isEmpty()) {
            item {
                EmptyKitchenState(text = "Aucune cuisine disponible pour ce compte.")
            }
        }

        items(restaurants) { restaurant ->
            KitchenRestaurantCard(
                restaurant = restaurant,
                onClick = { onRestaurantSelected(restaurant.id) }
            )
        }
    }
}

@Composable
private fun KitchenOrdersContent(
    uiState: KitchenDashboardUiState,
    onChangeRestaurant: () -> Unit,
    onUpdateOrderStatus: (Order, OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.selectedRestaurant?.name.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Commandes cuisine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(onClick = onChangeRestaurant) {
                    Text("Changer")
                }
            }
        }

        item {
            KitchenMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            SectionTitle(text = "File cuisine")
        }

        if (uiState.kitchenOrders.isEmpty()) {
            item {
                EmptyKitchenState(text = "Aucune commande cuisine a traiter.")
            }
        }

        items(uiState.kitchenOrders) { order ->
            KitchenOrderCard(
                order = order,
                isSaving = uiState.isSaving,
                onUpdateStatus = { status -> onUpdateOrderStatus(order, status) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun KitchenOrderCard(
    order: Order,
    isSaving: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    val nextStatus = order.status.nextChefStatus()

    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Commande #${order.id.takeLast(6)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${order.items.sumOf { it.quantity }} article(s) - ${order.totalPrice.asDhPrice()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            order.items.forEach { item ->
                Text(
                    text = "${item.quantity} x ${item.foodItem.name}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (item.specialInstruction.isNotBlank()) {
                    Text(
                        text = "Instruction: ${item.specialInstruction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (order.status == OrderStatus.READY_FOR_PICKUP && order.pickupCode.isNotBlank()) {
                PickupCodeCard(
                    orderId = order.id,
                    pickupCode = order.pickupCode,
                    title = "Code de retrait",
                    subtitle = "Le livreur valide ce code avant de partir."
                )
            }

            if (nextStatus != null) {
                ChezVousButton(
                    text = nextStatus.chefActionLabel(),
                    loadingText = "Mise a jour...",
                    isLoading = isSaving,
                    onClick = { onUpdateStatus(nextStatus) }
                )
            }
        }
    }
}

@Composable
private fun KitchenRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    ChezVousCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = restaurant.cuisineType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun KitchenMessageArea(
    message: String?,
    errorMessage: String?
) {
    val text = errorMessage ?: message ?: return
    val isError = errorMessage != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(ChezVousSpacing.sm),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

@Composable
private fun EmptyKitchenState(text: String) {
    ChezVousCard {
        Text(
            text = text,
            modifier = Modifier.padding(ChezVousSpacing.md),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KitchenLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun KitchenMessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun OrderStatus.chefActionLabel(): String {
    return when (this) {
        OrderStatus.PREPARING -> "Commencer la preparation"
        OrderStatus.READY_FOR_PICKUP -> "Marquer pret au retrait"
        else -> customerLabel()
    }
}
