package com.example.chezvous.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.repository.DriverRepository
import com.example.chezvous.data.repository.OrderRepository
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
    val isCancelling: Boolean = false
) {
    val canCancel: Boolean
        get() = order?.status == OrderStatus.PENDING ||
                order?.status == OrderStatus.CONFIRMED
}

class OrderTrackingViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val driverRepository = DriverRepository()
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
