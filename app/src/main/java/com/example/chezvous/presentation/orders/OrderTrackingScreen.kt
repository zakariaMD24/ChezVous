package com.example.chezvous.presentation.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.PaymentStatus
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.DriverCard
import com.example.chezvous.ui.components.OrderStatusChip
import com.example.chezvous.ui.components.OrderStatusStepper
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing
import kotlinx.coroutines.delay

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val viewModel: OrderTrackingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    LaunchedEffect(uiState.actionMessage) {
        if (uiState.actionMessage != null) {
            delay(2500)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Suivi",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.order == null -> {
                TrackingMessageState(
                    title = "Commande introuvable",
                    message = uiState.errorMessage ?: "Impossible de charger cette commande.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            else -> {
                OrderTrackingContent(
                    uiState = uiState,
                    onCancelOrder = viewModel::cancelOrder,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun OrderTrackingContent(
    uiState: OrderTrackingUiState,
    onCancelOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = uiState.order ?: return

    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            OrderHeaderCard(order = order)
        }

        item {
            OrderStatusStepper(status = order.status)
        }

        item {
            DriverCard(
                driver = uiState.driver,
                estimatedDeliveryTime = order.estimatedDeliveryTime
            )
        }

        item {
            SectionTitle(text = "Articles")
        }

        items(order.items) { item ->
            TrackingCartItemRow(item = item)
        }

        item {
            DeliveryAndPaymentCard(order = order)
        }

        if (uiState.actionMessage != null) {
            item {
                MessageSurface(
                    text = uiState.actionMessage,
                    isError = false
                )
            }
        }

        if (uiState.errorMessage != null) {
            item {
                MessageSurface(
                    text = uiState.errorMessage,
                    isError = true
                )
            }
        }

        if (uiState.canCancel) {
            item {
                ChezVousButton(
                    text = "Annuler la commande",
                    loadingText = "Annulation...",
                    isLoading = uiState.isCancelling,
                    onClick = onCancelOrder
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OrderHeaderCard(order: Order) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = order.restaurantName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Commande #${order.id.takeLast(6)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            Text(
                text = "Temps estime: ${order.estimatedDeliveryTime.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrackingCartItemRow(item: CartItem) {
    ChezVousCard {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.foodItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Quantite: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.selectedExtras.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.extras_summary,
                            item.selectedExtras.joinToString { it.name }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (item.removedIngredients.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.removed_ingredients_summary,
                            item.removedIngredients.joinToString()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (item.spiceLevel.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.spice_summary, item.spiceLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (item.specialInstruction.isNotBlank()) {
                    Text(
                        text = stringResource(
                            R.string.item_instruction_note,
                            item.specialInstruction
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = item.totalPrice.asDhPrice(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DeliveryAndPaymentCard(order: Order) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Livraison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = order.deliveryAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SummaryLine("Paiement", order.paymentStatus.customerLabel())
            SummaryLine("Sous-total", order.subtotal.asDhPrice())
            SummaryLine("Livraison", order.deliveryFee.asDhPrice())
            SummaryLine("Total", order.totalPrice.asDhPrice(), strong = true)
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal
        )

        Text(
            text = value,
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            color = if (strong) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun MessageSurface(
    text: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
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
private fun TrackingMessageState(
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
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(54.dp),
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

private fun PaymentStatus.customerLabel(): String {
    return when (this) {
        PaymentStatus.PENDING -> "En attente"
        PaymentStatus.PAID -> "Paye"
        PaymentStatus.FAILED -> "Refuse"
        PaymentStatus.CASH_ON_DELIVERY -> "Paiement a la livraison"
    }
}
