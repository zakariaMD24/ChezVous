package com.example.chezvous.presentation.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.CartItemCard
import com.example.chezvous.ui.components.CartSummaryCard
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.SectionTitle
import com.example.chezvous.ui.components.asDhPrice
import com.example.chezvous.ui.components.chezVousScreenPadding
import kotlinx.coroutines.delay

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckoutReady: () -> Unit = {}
) {
    val viewModel: CartViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            delay(2500)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = "Panier",
                onBack = onBack,
                actions = {
                    if (!uiState.isEmpty) {
                        TextButton(onClick = viewModel::clearCart) {
                            Text("Vider")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isEmpty) {
            EmptyCartState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .chezVousScreenPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    CartRestaurantHeader(uiState = uiState)
                }

                item {
                    SectionTitle(text = "Votre commande")
                }

                items(uiState.items) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        onIncrease = {
                            viewModel.increaseQuantity(cartItem.lineId)
                        },
                        onDecrease = {
                            viewModel.decreaseQuantity(cartItem.lineId)
                        },
                        onRemove = {
                            viewModel.removeItem(cartItem.lineId)
                        },
                        onInstructionChange = { instruction ->
                            viewModel.updateSpecialInstruction(
                                lineId = cartItem.lineId,
                                instruction = instruction
                            )
                        }
                    )
                }

                item {
                    CartSummaryCard(
                        subtotal = uiState.subtotal,
                        deliveryFee = uiState.deliveryFee,
                        total = uiState.total,
                        minimumOrder = uiState.minimumOrder,
                        remainingForMinimum = uiState.remainingForMinimum,
                        canCheckout = uiState.canCheckout,
                        onCheckout = {
                            onCheckoutReady()
                        }
                    )
                }

                if (uiState.message != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = uiState.message.orEmpty(),
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CartRestaurantHeader(uiState: CartUiState) {
    Column {
        Text(
            text = uiState.restaurant?.name ?: "Restaurant",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Commande minimum ${uiState.minimumOrder.asDhPrice()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.RemoveShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Votre panier est vide",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Ajoutez des plats depuis le menu d'un restaurant.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
