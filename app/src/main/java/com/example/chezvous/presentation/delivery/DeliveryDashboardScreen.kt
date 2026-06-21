package com.example.chezvous.presentation.delivery

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
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.OrderStatusChip
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.components.customerLabel
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

@Composable
fun DeliveryDashboardScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    val viewModel: DeliveryDashboardViewModel = viewModel()
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
                title = "Espace livreur",
                onBack = if (showBackButton) onBack else null
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingDeliveryState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            !uiState.isAuthorized -> {
                DeliveryMessageState(
                    title = "Acces livreur requis",
                    message = uiState.errorMessage.orEmpty(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            else -> {
                DeliveryDashboardContent(
                    uiState = uiState,
                    onAvailabilityChange = viewModel::updateAvailability,
                    onUpdateOrderStatus = viewModel::updateOrderStatus,
                    onValidatePickup = viewModel::validatePickup,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DeliveryDashboardContent(
    uiState: DeliveryDashboardUiState,
    onAvailabilityChange: (Boolean) -> Unit,
    onUpdateOrderStatus: (Order, OrderStatus) -> Unit,
    onValidatePickup: (Order, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            DriverStatusCard(
                uiState = uiState,
                onAvailabilityChange = onAvailabilityChange
            )
        }

        item {
            DeliveryMessageArea(
                message = uiState.message,
                errorMessage = uiState.errorMessage
            )
        }

        item {
            SectionTitle(text = "Livraisons actives")
        }

        if (uiState.activeOrders.isEmpty()) {
            item {
                EmptyDeliveryState(text = "Aucune livraison active pour le moment.")
            }
        }

        items(uiState.activeOrders) { order ->
            DriverOrderCard(
                order = order,
                isSaving = uiState.isSaving,
                onUpdateOrderStatus = { nextStatus ->
                    onUpdateOrderStatus(order, nextStatus)
                },
                onValidatePickup = { pickupCode ->
                    onValidatePickup(order, pickupCode)
                }
            )
        }

        if (uiState.completedOrders.isNotEmpty()) {
            item {
                SectionTitle(text = "Historique livreur")
            }

            items(uiState.completedOrders) { order ->
                DriverOrderCard(
                    order = order,
                    isSaving = uiState.isSaving,
                    onUpdateOrderStatus = {},
                    onValidatePickup = {}
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DriverStatusCard(
    uiState: DeliveryDashboardUiState,
    onAvailabilityChange: (Boolean) -> Unit
) {
    ChezVousCard {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeliveryDining,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.driver?.fullName ?: "Livreur",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = uiState.driver?.vehicleType.orEmpty().ifBlank { "Vehicule non defini" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Disponible",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Switch(
                checked = uiState.driver?.isAvailable ?: false,
                enabled = !uiState.isSaving,
                onCheckedChange = onAvailabilityChange
            )
        }
    }
}

@Composable
private fun DriverOrderCard(
    order: Order,
    isSaving: Boolean,
    onUpdateOrderStatus: (OrderStatus) -> Unit,
    onValidatePickup: (String) -> Unit
) {
    val nextStatus = order.status.nextDriverStatus()
    var pickupCode by remember(order.id) { mutableStateOf("") }

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
                        text = order.restaurantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Commande #${order.id.takeLast(6)} - ${order.totalPrice.asDhPrice()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = order.deliveryAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            order.items.take(3).forEach { item ->
                DriverOrderItemLine(item = item)
            }

            if (order.items.size > 3) {
                Text(
                    text = "+ ${order.items.size - 3} autre(s) article(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.status == OrderStatus.READY_FOR_PICKUP) {
                ChezVousTextField(
                    value = pickupCode,
                    onValueChange = { pickupCode = it.uppercase().take(6) },
                    label = "Code de retrait"
                )

                ChezVousButton(
                    text = "Valider le retrait",
                    loadingText = "Validation...",
                    isLoading = isSaving,
                    onClick = { onValidatePickup(pickupCode) }
                )
            } else if (nextStatus != null) {
                ChezVousButton(
                    text = nextStatus.driverActionLabel(),
                    loadingText = "Mise a jour...",
                    isLoading = isSaving,
                    onClick = { onUpdateOrderStatus(nextStatus) }
                )
            }
        }
    }
}

@Composable
private fun DriverOrderItemLine(item: CartItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${item.quantity} x ${item.foodItem.name}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = item.totalPrice.asDhPrice(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DeliveryMessageArea(
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
private fun EmptyDeliveryState(text: String) {
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
private fun LoadingDeliveryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DeliveryMessageState(
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
            imageVector = Icons.Outlined.DeliveryDining,
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

private fun OrderStatus.driverActionLabel(): String {
    return when (this) {
        OrderStatus.ON_THE_WAY -> "Je pars livrer"
        OrderStatus.DELIVERED -> "Marquer livree"
        else -> customerLabel()
    }
}
