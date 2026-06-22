package com.example.chezvous.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.RestaurantReview
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.DriverRepository
import com.example.chezvous.data.repository.OrderRepository
import com.example.chezvous.data.repository.RestaurantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderTrackingUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val driver: Driver? = null,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val isCancelling: Boolean = false,
    val isReviewSaving: Boolean = false
) {
    val canCancel: Boolean
        get() = order?.status == OrderStatus.PENDING ||
                order?.status == OrderStatus.ACCEPTED
}

class OrderTrackingViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private val driverRepository = DriverRepository()
    private val restaurantRepository = RestaurantRepository()
    private var currentOrderId = ""
    private var orderJob: Job? = null
    private var driverJob: Job? = null

    private val _uiState = MutableStateFlow(OrderTrackingUiState())
    val uiState: StateFlow<OrderTrackingUiState> = _uiState

    fun loadOrder(orderId: String) {
        if (orderId.isBlank() || orderId == currentOrderId) return

        currentOrderId = orderId
        orderJob?.cancel()
        driverJob?.cancel()
        _uiState.value = OrderTrackingUiState(isLoading = true)

        orderJob = viewModelScope.launch {
            orderRepository.observeOrder(orderId).collect { order ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        order = order,
                        errorMessage = if (order == null) {
                            "Commande introuvable."
                        } else {
                            null
                        }
                    )
                }

                observeDriver(order?.driverId.orEmpty())
            }
        }
    }

    fun cancelOrder() {
        val order = _uiState.value.order ?: return
        if (!_uiState.value.canCancel) {
            _uiState.update {
                it.copy(actionMessage = "Cette commande ne peut plus etre annulee.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCancelling = true,
                    actionMessage = null,
                    errorMessage = null
                )
            }

            orderRepository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            actionMessage = "Commande annulee."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = error.message ?: "Impossible d'annuler la commande."
                        )
                    }
                }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    fun submitReview(rating: Int, comment: String) {
        val order = _uiState.value.order ?: return
        val userId = authRepository.currentUserId().orEmpty()

        if (order.status != OrderStatus.DELIVERED || userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La note sera disponible apres livraison.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isReviewSaving = true, errorMessage = null, actionMessage = null) }

            restaurantRepository.submitRestaurantReview(
                RestaurantReview(
                    orderId = order.id,
                    restaurantId = order.restaurantId,
                    userId = userId,
                    customerId = userId,
                    customerName = order.customerName,
                    rating = rating,
                    comment = comment
                )
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isReviewSaving = false,
                            actionMessage = "Merci pour votre avis."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isReviewSaving = false,
                            errorMessage = error.message ?: "Impossible d'enregistrer votre avis."
                        )
                    }
                }
        }
    }

    private fun observeDriver(driverId: String) {
        driverJob?.cancel()

        if (driverId.isBlank()) {
            _uiState.update { it.copy(driver = null) }
            return
        }

        driverJob = viewModelScope.launch {
            driverRepository.observeDriver(driverId).collect { driver ->
                _uiState.update { it.copy(driver = driver) }
            }
        }
    }
}
