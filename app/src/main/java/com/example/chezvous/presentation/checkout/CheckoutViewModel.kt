package com.example.chezvous.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.PaymentStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.DEFAULT_DELIVERY_FEE
import com.example.chezvous.data.repository.OrderRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckoutPaymentMethod(
    val label: String,
    val description: String,
    val firestoreValue: String
) {
    CARD(
        label = "Carte bancaire",
        description = "Paiement en ligne simule",
        firestoreValue = "CARD"
    ),
    CASH_ON_DELIVERY(
        label = "Paiement a la livraison",
        description = "Regler la commande a la reception",
        firestoreValue = "CASH_ON_DELIVERY"
    )
}

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val restaurant: Restaurant? = null,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val deliveryAddress: String = "",
    val paymentMethod: CheckoutPaymentMethod = CheckoutPaymentMethod.CARD,
    val cardHolder: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val isLoading: Boolean = false,
    val orderId: String? = null,
    val errorMessage: String? = null
) {
    val isCartEmpty: Boolean
        get() = items.isEmpty()

    val isSuccess: Boolean
        get() = orderId != null

    val canConfirm: Boolean
        get() {
            val addressIsValid = deliveryAddress.trim().length >= 8
            val cardIsValid = paymentMethod != CheckoutPaymentMethod.CARD ||
                    (cardHolder.trim().isNotBlank() &&
                            cardNumber.onlyDigits().length in 13..16 &&
                            cardExpiry.isValidCardExpiry() &&
                            cardCvv.onlyDigits().length in 3..4)

            return !isLoading &&
                    !isSuccess &&
                    !isCartEmpty &&
                    restaurant != null &&
                    addressIsValid &&
                    cardIsValid
        }
}

class CheckoutViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val orderRepository = OrderRepository()
    private var didPrefillAddress = false

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                CartRepository.cartItems,
                CartRepository.restaurant
            ) { items, restaurant ->
                items to restaurant
            }.collect { (items, restaurant) ->
                val subtotal = items.sumOf { it.totalPrice }
                val deliveryFee = if (items.isEmpty()) 0.0 else DEFAULT_DELIVERY_FEE

                _uiState.update {
                    it.copy(
                        items = items,
                        restaurant = restaurant,
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        total = subtotal + deliveryFee
                    )
                }
            }
        }

        authRepository.currentUserId()?.let { userId ->
            viewModelScope.launch {
                userRepository.observeUser(userId).collect { user ->
                    val address = user?.address.orEmpty()
                    if (!didPrefillAddress && address.isNotBlank()) {
                        didPrefillAddress = true
                        _uiState.update { it.copy(deliveryAddress = address) }
                    }
                }
            }
        }

    }

    fun onDeliveryAddressChange(value: String) {
        didPrefillAddress = true
        _uiState.update {
            it.copy(
                deliveryAddress = value,
                errorMessage = null
            )
        }
    }

    fun onPaymentMethodSelected(method: CheckoutPaymentMethod) {
        _uiState.update {
            it.copy(
                paymentMethod = method,
                errorMessage = null
            )
        }
    }

    fun onCardHolderChange(value: String) {
        _uiState.update { it.copy(cardHolder = value, errorMessage = null) }
    }

    fun onCardNumberChange(value: String) {
        _uiState.update {
            it.copy(
                cardNumber = value.filter { char -> char.isDigit() }.take(16),
                errorMessage = null
            )
        }
    }

    fun onCardExpiryChange(value: String) {
        val digits = value.onlyDigits().take(4)
        _uiState.update {
            it.copy(
                cardExpiry = digits.formatCardExpiry(),
                errorMessage = null
            )
        }
    }

    fun onCardCvvChange(value: String) {
        _uiState.update {
            it.copy(
                cardCvv = value.filter { char -> char.isDigit() }.take(4),
                errorMessage = null
            )
        }
    }

    fun confirmOrder() {
        val state = _uiState.value
        if (!state.canConfirm) {
            _uiState.update {
                it.copy(errorMessage = "Completez l'adresse et le paiement avant de confirmer.")
            }
            return
        }

        val userId = authRepository.currentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Connectez-vous pour confirmer la commande.")
            }
            return
        }

        val restaurant = state.restaurant
        if (restaurant == null) {
            _uiState.update {
                it.copy(errorMessage = "Restaurant introuvable pour cette commande.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            delay(900)

            val paymentStatus = when (state.paymentMethod) {
                CheckoutPaymentMethod.CARD -> PaymentStatus.PAID
                CheckoutPaymentMethod.CASH_ON_DELIVERY -> PaymentStatus.CASH_ON_DELIVERY
            }
            val order = Order(
                userId = userId,
                restaurantId = restaurant.id,
                restaurantName = restaurant.name,
                items = state.items,
                subtotal = state.subtotal,
                deliveryFee = state.deliveryFee,
                totalPrice = state.total,
                deliveryAddress = state.deliveryAddress.trim(),
                paymentMethod = state.paymentMethod.firestoreValue,
                paymentStatus = paymentStatus,
                status = OrderStatus.PENDING,
                driverId = "",
                estimatedDeliveryTime = restaurant.deliveryTime
            )

            orderRepository.createOrder(order)
                .onSuccess { orderId ->
                    CartRepository.clearCart()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orderId = orderId,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Impossible de creer la commande."
                        )
                    }
                }
        }
    }
}

private fun String.onlyDigits(): String {
    return filter { it.isDigit() }
}

private fun String.formatCardExpiry(): String {
    return if (length <= 2) {
        this
    } else {
        "${take(2)}/${drop(2)}"
    }
}

private fun String.isValidCardExpiry(): Boolean {
    val parts = split("/")
    if (parts.size != 2) return false

    val month = parts[0].toIntOrNull() ?: return false
    val year = parts[1].toIntOrNull() ?: return false

    return month in 1..12 && year in 0..99
}
