package com.example.chezvous.presentation.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.OrderRepository
import com.example.chezvous.data.repository.RestaurantRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KitchenDashboardUiState(
    val isLoading: Boolean = true,
    val isAuthorized: Boolean = false,
    val role: String = UserRoles.CUSTOMER,
    val managedRestaurantIds: List<String> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String = "",
    val orders: List<Order> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val selectedRestaurant: Restaurant?
        get() = restaurants.firstOrNull { it.id == selectedRestaurantId }

    val kitchenOrders: List<Order>
        get() = orders.filter {
            it.status == OrderStatus.CONFIRMED ||
                    it.status == OrderStatus.PREPARING ||
                    it.status == OrderStatus.READY_FOR_PICKUP
        }
}

class KitchenDashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val restaurantRepository = RestaurantRepository()
    private val orderRepository = OrderRepository()

    private var ordersJob: Job? = null
    private var allRestaurants = emptyList<Restaurant>()

    private val _uiState = MutableStateFlow(KitchenDashboardUiState())
    val uiState: StateFlow<KitchenDashboardUiState> = _uiState

    init {
        observeAccess()
        observeRestaurants()
    }

    fun selectRestaurant(restaurantId: String) {
        if (restaurantId.isBlank() || restaurantId == _uiState.value.selectedRestaurantId) return
        if (_uiState.value.restaurants.none { it.id == restaurantId }) {
            _uiState.update {
                it.copy(errorMessage = "Vous n'avez pas acces a ce restaurant.")
            }
            return
        }

        _uiState.update {
            it.copy(
                selectedRestaurantId = restaurantId,
                orders = emptyList(),
                message = null,
                errorMessage = null
            )
        }
        observeSelectedRestaurantOrders(restaurantId)
    }

    fun clearRestaurantSelection() {
        ordersJob?.cancel()
        _uiState.update {
            it.copy(
                selectedRestaurantId = "",
                orders = emptyList(),
                message = null,
                errorMessage = null
            )
        }
    }

    fun updateOrderStatus(order: Order, nextStatus: OrderStatus) {
        if (order.restaurantId !in _uiState.value.managedRestaurantIds ||
            nextStatus !in listOf(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP)
        ) {
            _uiState.update {
                it.copy(errorMessage = "Action cuisine non autorisee.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.updateOrderStatus(order.id, nextStatus)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Commande cuisine mise a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de modifier la commande."
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
            _uiState.value = KitchenDashboardUiState(
                isLoading = false,
                errorMessage = "Connectez-vous avec un compte cuisine."
            )
            return
        }

        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                val role = user?.role ?: UserRoles.CUSTOMER
                val managedRestaurantIds = user?.managedRestaurantIds.orEmpty()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = role,
                        managedRestaurantIds = managedRestaurantIds
                    )
                }
                refreshVisibleRestaurants()
            }
        }
    }

    private fun observeRestaurants() {
        viewModelScope.launch {
            restaurantRepository.observeRestaurants().collect { restaurants ->
                allRestaurants = restaurants
                refreshVisibleRestaurants()
            }
        }
    }

    private fun refreshVisibleRestaurants() {
        val state = _uiState.value
        val visibleRestaurants = allRestaurants.filter { it.id in state.managedRestaurantIds }
        val selectedRestaurantId = state.selectedRestaurantId
            .takeIf { restaurantId -> visibleRestaurants.any { it.id == restaurantId } }
            ?: visibleRestaurants.singleOrNull()?.id
            ?: ""
        val selectionChanged = selectedRestaurantId != state.selectedRestaurantId

        _uiState.update {
            it.copy(
                isAuthorized = UserRoles.canUseKitchenDashboard(state.role) &&
                        visibleRestaurants.isNotEmpty(),
                restaurants = visibleRestaurants,
                selectedRestaurantId = selectedRestaurantId,
                orders = if (selectionChanged) emptyList() else it.orders,
                errorMessage = when {
                    !UserRoles.canUseKitchenDashboard(state.role) ->
                        "Ce compte n'est pas un compte cuisine."
                    visibleRestaurants.isEmpty() ->
                        "Aucun restaurant cuisine n'est assigne a ce compte."
                    else -> null
                }
            )
        }

        if (selectedRestaurantId.isBlank()) {
            ordersJob?.cancel()
        } else if (selectionChanged) {
            observeSelectedRestaurantOrders(selectedRestaurantId)
        }
    }

    private fun observeSelectedRestaurantOrders(restaurantId: String) {
        ordersJob?.cancel()
        ordersJob = viewModelScope.launch {
            orderRepository.observeRestaurantOrders(restaurantId).collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }
}

fun OrderStatus.nextChefStatus(): OrderStatus? {
    return when (this) {
        OrderStatus.CONFIRMED -> OrderStatus.PREPARING
        OrderStatus.PREPARING -> OrderStatus.READY_FOR_PICKUP
        OrderStatus.PENDING,
        OrderStatus.READY_FOR_PICKUP,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED -> null
    }
}
