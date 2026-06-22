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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeliveryDashboardUiState(
    val isLoading: Boolean = true,
    val isAuthorized: Boolean = false,
    val currentUserId: String = "",
    val driverId: String = "",
    val driver: Driver? = null,
    val orders: List<Order> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val activeOrders: List<Order>
        get() {
            val driverKeys = driverKeys()
            val driverIsAvailable = driver?.isAvailable ?: true
            return orders.filter { order ->
                val assignedToDriver = order.driverId.trim() in driverKeys
                when (order.status) {
                    OrderStatus.READY_FOR_PICKUP -> {
                        assignedToDriver || (driverIsAvailable && order.driverId.trim().isBlank())
                    }
                    OrderStatus.PICKED_UP,
                    OrderStatus.ON_THE_WAY -> assignedToDriver
                    OrderStatus.PENDING,
                    OrderStatus.ACCEPTED,
                    OrderStatus.PREPARING,
                    OrderStatus.DELIVERED,
                    OrderStatus.CANCELLED -> false
                }
            }
        }

    val completedOrders: List<Order>
        get() {
            val driverKeys = driverKeys()
            return orders.filter {
                it.status == OrderStatus.DELIVERED && it.driverId.trim() in driverKeys
            }
        }

    fun driverKeys(): Set<String> {
        return setOf(currentUserId.trim(), driverId.trim())
            .filter { it.isNotBlank() }
            .toSet()
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
        val state = _uiState.value
        if (order.driverId.trim() !in state.driverKeys() || !status.isDriverWritableStatus()) {
            _uiState.update {
                it.copy(errorMessage = "Action non autorisee pour cette livraison.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.updateDriverOrderStatus(
                orderId = order.id,
                driverId = _uiState.value.driverId,
                currentUserId = _uiState.value.currentUserId,
                status = status
            )
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
        val state = _uiState.value
        val driverId = state.driverId
        val driverKeys = state.driverKeys()
        if (driverId.isBlank() || (order.driverId.isNotBlank() && order.driverId.trim() !in driverKeys)) {
            _uiState.update {
                it.copy(errorMessage = "Action non autorisee pour cette livraison.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.validatePickup(
                orderId = order.id,
                pickupCode = pickupCode,
                driverId = _uiState.value.driverId,
                currentUserId = _uiState.value.currentUserId
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Retrait valide. Commande prise en charge."
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

    fun refresh() {
        val state = _uiState.value
        val driverId = state.driverId
        if (driverId.isBlank()) return

        observeDriverData(state.currentUserId, driverId)
        _uiState.update { it.copy(message = "Liste actualisee.", errorMessage = null) }
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
                val role = UserRoles.safeRole(user?.role)
                val driverId = user?.driverId.orEmpty().ifBlank { userId }
                val isAuthorized = UserRoles.canUseDriverDashboard(role)

                _uiState.update {
                    it.copy(
                        isLoading = isAuthorized,
                        isAuthorized = isAuthorized,
                        currentUserId = userId,
                        driverId = driverId,
                        errorMessage = when {
                            role != UserRoles.DRIVER -> "Ce compte n'est pas un compte livreur."
                            else -> null
                        }
                    )
                }

                observeDriverData(
                    currentUserId = userId.takeIf { isAuthorized }.orEmpty(),
                    driverId = driverId.takeIf { isAuthorized }.orEmpty()
                )
            }
        }
    }

    private fun observeDriverData(
        currentUserId: String,
        driverId: String
    ) {
        driverJob?.cancel()
        ordersJob?.cancel()

        if (driverId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, driver = null, orders = emptyList()) }
            return
        }

        driverJob = viewModelScope.launch {
            driverRepository.observeDriver(driverId).collect { driver ->
                _uiState.update { it.copy(driver = driver) }
            }
        }

        ordersJob = viewModelScope.launch {
            orderRepository.observeDriverVisibleOrders(currentUserId, driverId).collect { orders ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        orders = orders
                    )
                }
            }
        }
    }
}

fun OrderStatus.nextDriverStatus(): OrderStatus? {
    return when (this) {
        OrderStatus.READY_FOR_PICKUP -> OrderStatus.PICKED_UP
        OrderStatus.PICKED_UP -> OrderStatus.ON_THE_WAY
        OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
        OrderStatus.PENDING,
        OrderStatus.ACCEPTED,
        OrderStatus.PREPARING,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED -> null
    }
}

private fun OrderStatus.isDriverWritableStatus(): Boolean {
    return this == OrderStatus.ON_THE_WAY || this == OrderStatus.DELIVERED
}
