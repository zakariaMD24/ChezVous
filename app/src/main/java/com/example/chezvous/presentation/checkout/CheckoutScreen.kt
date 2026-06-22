package com.example.chezvous.presentation.checkout

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousCard
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.PaymentMethodCard
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit
) {
    val viewModel: CheckoutViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Paiement",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        when {
            uiState.isSuccess -> {
                CheckoutSuccessState(
                    orderId = uiState.orderId.orEmpty(),
                    onTrackOrder = {
                        uiState.orderId?.let(onOrderCreated)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            uiState.isCartEmpty -> {
                EmptyCheckoutState(
                    onBack = onBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                )
            }

            else -> {
                CheckoutContent(
                    uiState = uiState,
                    onDeliveryAddressChange = viewModel::onDeliveryAddressChange,
                    onDeliveryNoteChange = viewModel::onDeliveryNoteChange,
                    onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
                    onCardHolderChange = viewModel::onCardHolderChange,
                    onCardNumberChange = viewModel::onCardNumberChange,
                    onCardExpiryChange = viewModel::onCardExpiryChange,
                    onCardCvvChange = viewModel::onCardCvvChange,
                    onConfirmOrder = viewModel::confirmOrder,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    uiState: CheckoutUiState,
    onDeliveryAddressChange: (String) -> Unit,
    onDeliveryNoteChange: (String) -> Unit,
    onPaymentMethodSelected: (CheckoutPaymentMethod) -> Unit,
    onCardHolderChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.chezVousScreenPadding(),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            RestaurantCheckoutHeader(uiState = uiState)
        }

        item {
            SectionTitle(text = "Adresse de livraison")
            ChezVousTextField(
                value = uiState.deliveryAddress,
                onValueChange = onDeliveryAddressChange,
                label = "Adresse complete",
                leadingIcon = Icons.Outlined.LocationOn,
                singleLine = false,
                imeAction = ImeAction.Next,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

            ChezVousTextField(
                value = uiState.deliveryNote,
                onValueChange = onDeliveryNoteChange,
                label = "Note de livraison (optionnel)",
                singleLine = false,
                imeAction = ImeAction.Next,
                enabled = !uiState.isLoading
            )
        }

        item {
            SectionTitle(text = "Methode de paiement")
            Column(
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
            ) {
                PaymentMethodCard(
                    title = CheckoutPaymentMethod.CARD.label,
                    description = CheckoutPaymentMethod.CARD.description,
                    icon = Icons.Outlined.CreditCard,
                    selected = uiState.paymentMethod == CheckoutPaymentMethod.CARD,
                    enabled = !uiState.isLoading,
                    onClick = { onPaymentMethodSelected(CheckoutPaymentMethod.CARD) }
                )

                PaymentMethodCard(
                    title = CheckoutPaymentMethod.CASH_ON_DELIVERY.label,
                    description = CheckoutPaymentMethod.CASH_ON_DELIVERY.description,
                    icon = Icons.Outlined.Payments,
                    selected = uiState.paymentMethod == CheckoutPaymentMethod.CASH_ON_DELIVERY,
                    enabled = !uiState.isLoading,
                    onClick = {
                        onPaymentMethodSelected(CheckoutPaymentMethod.CASH_ON_DELIVERY)
                    }
                )
            }
        }

        if (uiState.paymentMethod == CheckoutPaymentMethod.CARD) {
            item {
                CardPaymentFields(
                    uiState = uiState,
                    onCardHolderChange = onCardHolderChange,
                    onCardNumberChange = onCardNumberChange,
                    onCardExpiryChange = onCardExpiryChange,
                    onCardCvvChange = onCardCvvChange
                )
            }
        }

        item {
            CheckoutSummaryCard(uiState = uiState)
        }

        if (uiState.errorMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        item {
            ChezVousButton(
                text = "Confirmer la commande",
                loadingText = "Paiement en cours...",
                isLoading = uiState.isLoading,
                enabled = uiState.canConfirm,
                onClick = onConfirmOrder
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RestaurantCheckoutHeader(uiState: CheckoutUiState) {
    Column {
        Text(
            text = uiState.restaurant?.name ?: "Restaurant",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "${uiState.items.sumOf { it.quantity }} article(s) dans votre commande",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CardPaymentFields(
    uiState: CheckoutUiState,
    onCardHolderChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
    ) {
        SectionTitle(text = "Carte bancaire")
        ChezVousTextField(
            value = uiState.cardHolder,
            onValueChange = onCardHolderChange,
            label = "Nom sur la carte",
            leadingIcon = Icons.Outlined.CreditCard,
            enabled = !uiState.isLoading
        )

        ChezVousTextField(
            value = uiState.cardNumber,
            onValueChange = onCardNumberChange,
            label = "Numero de carte",
            leadingIcon = Icons.Outlined.CreditCard,
            keyboardType = KeyboardType.Number,
            enabled = !uiState.isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            ChezVousTextField(
                value = uiState.cardExpiry,
                onValueChange = onCardExpiryChange,
                label = "MM/AA",
                keyboardType = KeyboardType.Text,
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            )

            ChezVousTextField(
                value = uiState.cardCvv,
                onValueChange = onCardCvvChange,
                label = "CVV",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CheckoutSummaryCard(uiState: CheckoutUiState) {
    ChezVousCard {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Resume de commande",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            SummaryRow("Sous-total", uiState.subtotal.asDhPrice())
            SummaryRow("Livraison", uiState.deliveryFee.asDhPrice())
            SummaryRow(
                label = "Total a payer",
                value = uiState.total.asDhPrice(),
                strong = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = if (strong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = if (strong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
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
private fun CheckoutSuccessState(
    orderId: String,
    onTrackOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Commande confirmee",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Reference: $orderId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ChezVousButton(
            text = "Suivre la commande",
            onClick = onTrackOrder
        )
    }
}

@Composable
private fun EmptyCheckoutState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aucune commande a payer",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Ajoutez des plats au panier avant de payer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ChezVousButton(
            text = "Retour au panier",
            onClick = onBack
        )
    }
}
