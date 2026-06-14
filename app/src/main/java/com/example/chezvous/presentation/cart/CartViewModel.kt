package com.example.chezvous.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.DEFAULT_DELIVERY_FEE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val restaurant: Restaurant? = null,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val minimumOrder: Double = 0.0,
    val remainingForMinimum: Double = 0.0,
    val canCheckout: Boolean = false,
    val message: String? = null
) {
    val isEmpty: Boolean
        get() = items.isEmpty()
}

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                CartRepository.cartItems,
                CartRepository.restaurant
            ) { items, restaurant ->
                items to restaurant
            }.collect { (items, restaurant) ->
                _uiState.update { currentState ->
                    buildCartUiState(
                        items = items,
                        restaurant = restaurant,
                        message = currentState.message
                    )
                }
            }
        }
    }

    fun increaseQuantity(foodItemId: String) {
        CartRepository.increaseQuantity(foodItemId)
    }

    fun decreaseQuantity(foodItemId: String) {
        CartRepository.decreaseQuantity(foodItemId)
    }

    fun removeItem(foodItemId: String) {
        CartRepository.removeItem(foodItemId)
    }

    fun clearCart() {
        CartRepository.clearCart()
    }

    fun onCheckoutClick() {
        _uiState.update {
            it.copy(message = "Le paiement sera ajoute dans la phase suivante.")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun buildCartUiState(
        items: List<CartItem>,
        restaurant: Restaurant?,
        message: String?
    ): CartUiState {
        val subtotal = items.sumOf { it.totalPrice }
        val deliveryFee = if (items.isEmpty()) 0.0 else DEFAULT_DELIVERY_FEE
        val minimumOrder = restaurant?.minimumOrder ?: 0.0
        val remainingForMinimum = max(0.0, minimumOrder - subtotal)

        return CartUiState(
            items = items,
            restaurant = restaurant,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = subtotal + deliveryFee,
            minimumOrder = minimumOrder,
            remainingForMinimum = remainingForMinimum,
            canCheckout = items.isNotEmpty() && remainingForMinimum <= 0.0,
            message = message
        )
    }
}
