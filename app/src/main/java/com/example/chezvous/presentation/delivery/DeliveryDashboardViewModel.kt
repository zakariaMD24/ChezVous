package com.example.chezvous.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.DriverRepository
import com.example.chezvous.data.repository.OrderRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeliveryDashboardUiState(
    val isLoading: Boolean = true,
    val isAuthorized: Boolean = false,
    val driverId: String = "",
    val driver: Driver? = null,
    val orders: List<Order> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val activeOrders: List<Order>
        get() = orders.filter {
            it.status == OrderStatus.READY_FOR_PICKUP ||
                    it.status == OrderStatus.ON_THE_WAY
        }

    val completedOrders: List<Order>
        get() = orders.filter {
            it.status == OrderStatus.DELIVERED || it.status == OrderStatus.CANCELLED
        }
}

class DeliveryDashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val driverRepository = DriverRepository()
    private val orderRepository = OrderRepository()

    private var driverJob: Job? = null
    private var ordersJob: Job? = null

    private val _uiState = MutableStateFlow(DeliveryDashboardUiState())
    val uiState: StateFlow<DeliveryDashboardUiState> = _uiState

    init {
        observeAccess()
    }

    fun updateOrderStatus(order: Order, status: OrderStatus) {
        if (order.driverId != _uiState.value.driverId || !status.isDriverWritableStatus()) {
            _uiState.update {
                it.copy(errorMessage = "Action non autorisee pour cette livraison.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.updateOrderStatus(order.id, status)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Livraison mise a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de mettre a jour la livraison."
                        )
                    }
                }
        }
    }

    fun validatePickup(order: Order, pickupCode: String) {
        val driverId = _uiState.value.driverId
        if (driverId.isBlank() || (order.driverId.isNotBlank() && order.driverId != driverId)) {
            _uiState.update {
                it.copy(errorMessage = "Action non autorisee pour cette livraison.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.validatePickup(order.id, pickupCode, _uiState.value.driverId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Retrait valide. Livraison en route."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de valider le retrait."
                        )
                    }
                }
        }
    }

    fun updateAvailability(isAvailable: Boolean) {
        val driverId = _uiState.value.driverId
        if (driverId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            driverRepository.updateDriverAvailability(driverId, isAvailable)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Disponibilite mise a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de changer la disponibilite."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, errorMessage = null) }
    }

    private fun observeAccess() {
        val userId = authRepository.currentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.value = DeliveryDashboardUiState(
                isLoading = false,
                errorMessage = "Connectez-vous avec un compte livreur."
            )
            return
        }

        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                val role = user?.role ?: UserRoles.CUSTOMER
                val driverId = user?.driverId.orEmpty()
                val isAuthorized = UserRoles.canUseDriverDashboard(role) && driverId.isNotBlank()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthorized = isAuthorized,
                        driverId = driverId,
                        errorMessage = when {
                            role != UserRoles.DRIVER -> "Ce compte n'est pas un compte livreur."
                            driverId.isBlank() -> "Ce livreur n'est pas encore lie a une fiche livreur."
                            else -> null
                        }
                    )
                }

                observeDriverData(driverId.takeIf { isAuthorized }.orEmpty())
            }
        }
    }

    private fun observeDriverData(driverId: String) {
        driverJob?.cancel()
        ordersJob?.cancel()

        if (driverId.isBlank()) {
            _uiState.update { it.copy(driver = null, orders = emptyList()) }
            return
        }

        driverJob = viewModelScope.launch {
            driverRepository.observeDriver(driverId).collect { driver ->
                _uiState.update { it.copy(driver = driver) }
            }
        }

        ordersJob = viewModelScope.launch {
            combine(
                orderRepository.observeDriverOrders(driverId),
                orderRepository.observeReadyForPickupOrders()
            ) { assignedOrders, readyOrders ->
                (assignedOrders + readyOrders.filter { order ->
                    order.driverId.isBlank() || order.driverId == driverId
                })
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }
            }.collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }
}

fun OrderStatus.nextDriverStatus(): OrderStatus? {
    return when (this) {
        OrderStatus.READY_FOR_PICKUP -> OrderStatus.ON_THE_WAY
        OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
        OrderStatus.PENDING,
        OrderStatus.CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED -> null
    }
}

private fun OrderStatus.isDriverWritableStatus(): Boolean {
    return this == OrderStatus.DELIVERED
}
