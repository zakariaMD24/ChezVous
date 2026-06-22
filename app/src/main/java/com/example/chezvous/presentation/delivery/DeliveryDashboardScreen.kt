package com.example.chezvous.presentation.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.PaymentStatus
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.OrderStatusChip
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
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
                title = stringResource(R.string.delivery_space),
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
                    title = stringResource(R.string.delivery_access_required),
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
                    onRefresh = viewModel::refresh,
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
    onRefresh: () -> Unit,
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
            SectionTitle(text = stringResource(R.string.delivery_active_orders))
        }

        if (uiState.activeOrders.isEmpty()) {
            item {
                EmptyDeliveryState(
                    text = stringResource(R.string.delivery_no_active_orders),
                    onRefresh = onRefresh
                )
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
                SectionTitle(text = stringResource(R.string.delivery_history))
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
    val missingVehicleText = stringResource(R.string.delivery_vehicle_missing)

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
                    text = uiState.driver?.fullName ?: stringResource(R.string.delivery_driver_fallback),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = uiState.driver?.vehicleType.orEmpty().ifBlank { missingVehicleText },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = stringResource(R.string.available),
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
    val missingAddressText = stringResource(R.string.delivery_address_missing)
    val missingCustomerText = stringResource(R.string.delivery_customer_fallback)
    val missingPhoneText = stringResource(R.string.delivery_phone_missing)
    val restaurantFallbackText = stringResource(R.string.restaurant)

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
                        text = order.restaurantName.ifBlank { restaurantFallbackText },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = stringResource(
                            R.string.delivery_order_title,
                            order.id.takeLast(6),
                            order.totalPrice.asDhPrice()
                        ),
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
                    text = order.deliveryAddress.ifBlank { missingAddressText },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            DeliveryInfoLine(
                label = stringResource(R.string.delivery_customer),
                value = order.customerName.ifBlank { missingCustomerText }
            )

            DeliveryInfoLine(
                label = stringResource(R.string.delivery_phone),
                value = order.customerPhone.ifBlank { missingPhoneText }
            )

            if (order.deliveryNote.isNotBlank()) {
                DeliveryInfoLine(
                    label = stringResource(R.string.delivery_note),
                    value = order.deliveryNote
                )
            }

            DeliveryInfoLine(
                label = stringResource(R.string.payment),
                value = "${order.paymentMethod.paymentMethodLabel()} - ${order.paymentStatus.paymentStatusLabel()}"
            )

            order.items.take(3).forEach { item ->
                DriverOrderItemLine(item = item)
            }

            if (order.items.size > 3) {
                Text(
                    text = stringResource(R.string.delivery_more_items, order.items.size - 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.status == OrderStatus.READY_FOR_PICKUP) {
                ChezVousTextField(
                    value = pickupCode,
                    onValueChange = { pickupCode = it.uppercase().take(6) },
                    label = stringResource(R.string.pickup_code_title)
                )

                ChezVousButton(
                    text = stringResource(R.string.delivery_validate_pickup),
                    loadingText = stringResource(R.string.delivery_validating),
                    isLoading = isSaving,
                    onClick = { onValidatePickup(pickupCode) }
                )
            } else if (nextStatus != null) {
                ChezVousButton(
                    text = stringResource(nextStatus.driverActionLabelRes()),
                    loadingText = stringResource(R.string.delivery_updating),
                    isLoading = isSaving,
                    onClick = { onUpdateOrderStatus(nextStatus) }
                )
            }
        }
    }
}

@Composable
private fun DeliveryInfoLine(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
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
private fun String.paymentMethodLabel(): String {
    val unknown = stringResource(R.string.payment_method_unknown)
    return when (this) {
        "CARD_SIMULATED", "CARD" -> stringResource(R.string.payment_method_card_simulated)
        "CASH_ON_DELIVERY" -> stringResource(R.string.payment_method_cash)
        "" -> unknown
        else -> this
    }
}

@Composable
private fun PaymentStatus.paymentStatusLabel(): String {
    return when (this) {
        PaymentStatus.PENDING -> stringResource(R.string.payment_pending)
        PaymentStatus.PENDING_CASH -> stringResource(R.string.payment_pending_cash)
        PaymentStatus.PAID -> stringResource(R.string.payment_paid)
        PaymentStatus.PAID_SIMULATED -> stringResource(R.string.payment_paid_simulated)
        PaymentStatus.FAILED -> stringResource(R.string.payment_failed)
        PaymentStatus.CASH_ON_DELIVERY -> stringResource(R.string.payment_cash_on_delivery)
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
private fun EmptyDeliveryState(
    text: String,
    onRefresh: () -> Unit
) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(ChezVousSpacing.xs))
                Text(stringResource(R.string.refresh))
            }
        }
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

private fun OrderStatus.driverActionLabelRes(): Int {
    return when (this) {
        OrderStatus.PICKED_UP -> R.string.delivery_action_picked_up
        OrderStatus.ON_THE_WAY -> R.string.delivery_action_on_the_way
        OrderStatus.DELIVERED -> R.string.delivery_action_delivered
        else -> R.string.update
    }
}
