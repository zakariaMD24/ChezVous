package com.example.chezvous.presentation.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
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

data class PartnerDashboardUiState(
    val isLoading: Boolean = true,
    val isAuthorized: Boolean = false,
    val role: String = UserRoles.CUSTOMER,
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String = "",
    val orders: List<Order> = emptyList(),
    val menuItems: List<FoodItem> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val selectedRestaurant: Restaurant?
        get() = restaurants.firstOrNull { it.id == selectedRestaurantId }
}

class PartnerDashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val restaurantRepository = RestaurantRepository()
    private val orderRepository = OrderRepository()

    private var ordersJob: Job? = null
    private var menuJob: Job? = null

    private val _uiState = MutableStateFlow(PartnerDashboardUiState())
    val uiState: StateFlow<PartnerDashboardUiState> = _uiState

    init {
        observeAccess()
        observeRestaurants()
    }

    fun selectRestaurant(restaurantId: String) {
        if (restaurantId.isBlank() || restaurantId == _uiState.value.selectedRestaurantId) return

        _uiState.update {
            it.copy(
                selectedRestaurantId = restaurantId,
                orders = emptyList(),
                menuItems = emptyList(),
                message = null,
                errorMessage = null
            )
        }

        observeSelectedRestaurantData(restaurantId)
    }

    fun updateOrderStatus(order: Order, nextStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            orderRepository.updateOrderStatus(order.id, nextStatus)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Statut mis a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de modifier le statut."
                        )
                    }
                }
        }
    }

    fun updateItemAvailability(
        foodItem: FoodItem,
        isAvailable: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            restaurantRepository.updateMenuItem(foodItem.copy(isAvailable = isAvailable))
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Disponibilite modifiee."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de modifier le plat."
                        )
                    }
                }
        }
    }

    fun saveMenuItem(
        itemId: String,
        name: String,
        description: String,
        category: String,
        priceText: String,
        imageUrl: String,
        extraOptions: List<CustomizationOption>,
        removableIngredientOptions: List<CustomizationOption>,
        spiceLevelOptions: List<CustomizationOption>
    ) {
        val selectedRestaurantId = _uiState.value.selectedRestaurantId
        val price = priceText.toPartnerPriceOrNull()
        val existingItem = _uiState.value.menuItems.firstOrNull { it.id == itemId }

        if (selectedRestaurantId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selectionnez un restaurant.") }
            return
        }

        if (name.trim().isBlank() || description.trim().isBlank() || price == null || price <= 0.0) {
            _uiState.update {
                it.copy(errorMessage = "Completez le nom, la description et un prix valide.")
            }
            return
        }

        val foodItem = FoodItem(
            id = itemId,
            restaurantId = selectedRestaurantId,
            name = name.trim(),
            description = description.trim(),
            price = price,
            category = category.trim().ifBlank { "Menu" },
            imageUrl = imageUrl.trim().ifBlank { existingItem?.imageUrl.orEmpty() },
            isAvailable = existingItem?.isAvailable ?: true,
            extraOptions = extraOptions,
            removableIngredients = removableIngredientOptions.map { it.name },
            spiceLevels = spiceLevelOptions.map { it.name },
            removableIngredientOptions = removableIngredientOptions,
            spiceLevelOptions = spiceLevelOptions
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val result = if (itemId.isBlank()) {
                restaurantRepository.createMenuItem(foodItem).map { Unit }
            } else {
                restaurantRepository.updateMenuItem(foodItem)
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Plat enregistre."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible d'enregistrer le plat."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                message = null,
                errorMessage = null
            )
        }
    }

    private fun observeAccess() {
        val userId = authRepository.currentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.value = PartnerDashboardUiState(
                isLoading = false,
                errorMessage = "Connectez-vous pour acceder a l'espace partenaire."
            )
            return
        }

        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                val role = user?.role ?: UserRoles.CUSTOMER
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = role,
                        isAuthorized = role == UserRoles.PARTNER || role == UserRoles.ADMIN,
                        errorMessage = if (role == UserRoles.PARTNER || role == UserRoles.ADMIN) {
                            null
                        } else {
                            "Votre compte n'a pas le role partenaire."
                        }
                    )
                }
            }
        }
    }

    private fun observeRestaurants() {
        viewModelScope.launch {
            restaurantRepository.observeRestaurants().collect { restaurants ->
                _uiState.update {
                    it.copy(restaurants = restaurants)
                }

                val currentSelection = _uiState.value.selectedRestaurantId
                val shouldSelectDefault = currentSelection.isBlank() ||
                        restaurants.none { it.id == currentSelection }
                if (restaurants.isNotEmpty() && shouldSelectDefault) {
                    selectRestaurant(restaurants.first().id)
                }
            }
        }
    }

    private fun observeSelectedRestaurantData(restaurantId: String) {
        ordersJob?.cancel()
        menuJob?.cancel()

        ordersJob = viewModelScope.launch {
            orderRepository.observeRestaurantOrders(restaurantId).collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }

        menuJob = viewModelScope.launch {
            restaurantRepository.observeMenuItems(restaurantId).collect { menuItems ->
                _uiState.update { it.copy(menuItems = menuItems) }
            }
        }
    }
}

private fun String.toPartnerPriceOrNull(): Double? {
    return replace(",", ".").toDoubleOrNull()
}
