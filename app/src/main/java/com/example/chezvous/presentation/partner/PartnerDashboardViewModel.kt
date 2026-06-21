package com.example.chezvous.presentation.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.model.User
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.DriverRepository
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
    val managedRestaurantIds: List<String> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String = "",
    val orders: List<Order> = emptyList(),
    val menuItems: List<FoodItem> = emptyList(),
    val drivers: List<Driver> = emptyList(),
    val users: List<User> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val userSearchQuery: String = ""
) {
    val selectedRestaurant: Restaurant?
        get() = restaurants.firstOrNull { it.id == selectedRestaurantId }
        
    val visibleUsers: List<User>
        get() = if (userSearchQuery.isBlank()) {
            users.filter { it.role != UserRoles.CUSTOMER }
        } else {
            users.filter { 
                it.email.contains(userSearchQuery, ignoreCase = true) || 
                it.fullName.contains(userSearchQuery, ignoreCase = true) 
            }
        }
}

class PartnerDashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val restaurantRepository = RestaurantRepository()
    private val orderRepository = OrderRepository()
    private val driverRepository = DriverRepository()

    private var ordersJob: Job? = null
    private var menuJob: Job? = null
    private var usersJob: Job? = null
    private var allRestaurants = emptyList<Restaurant>()

    private val _uiState = MutableStateFlow(PartnerDashboardUiState())
    val uiState: StateFlow<PartnerDashboardUiState> = _uiState

    init {
        observeAccess()
        observeRestaurants()
        observeDrivers()
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
                menuItems = emptyList(),
                message = null,
                errorMessage = null
            )
        }

        observeSelectedRestaurantData(restaurantId)
    }

    fun clearRestaurantSelection() {
        ordersJob?.cancel()
        menuJob?.cancel()
        _uiState.update {
            it.copy(
                selectedRestaurantId = "",
                orders = emptyList(),
                menuItems = emptyList(),
                message = null,
                errorMessage = null
            )
        }
    }

    fun updateOrderStatus(order: Order, nextStatus: OrderStatus) {
        if (!canManageRestaurant(order.restaurantId)) {
            _uiState.update {
                it.copy(errorMessage = "Vous ne pouvez pas modifier ce restaurant.")
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

    fun onUserSearchQueryChange(query: String) {
        _uiState.update { it.copy(userSearchQuery = query) }
    }

    fun updateItemAvailability(
        foodItem: FoodItem,
        isAvailable: Boolean
    ) {
        if (!canManageRestaurant(foodItem.restaurantId)) {
            _uiState.update {
                it.copy(errorMessage = "Vous ne pouvez pas modifier ce restaurant.")
            }
            return
        }

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
        isSpiceLevelEnabled: Boolean
    ) {
        val selectedRestaurantId = _uiState.value.selectedRestaurantId
        val price = priceText.toPartnerPriceOrNull()
        val existingItem = _uiState.value.menuItems.firstOrNull { it.id == itemId }

        if (selectedRestaurantId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selectionnez un restaurant.") }
            return
        }

        if (!canManageRestaurant(selectedRestaurantId)) {
            _uiState.update {
                it.copy(errorMessage = "Vous ne pouvez pas modifier ce restaurant.")
            }
            return
        }

        if (
            name.trim().isBlank() ||
            description.trim().isBlank() ||
            category.trim().isBlank() ||
            price == null ||
            price <= 0.0
        ) {
            _uiState.update {
                it.copy(errorMessage = "Completez le nom, la description, la categorie et un prix valide.")
            }
            return
        }

        val spiceOptions = if (isSpiceLevelEnabled) {
            existingItem?.spiceLevelOptions
                ?.takeIf { it.isNotEmpty() }
                ?: defaultSpiceLevelOptions()
        } else {
            emptyList()
        }

        val foodItem = FoodItem(
            id = itemId,
            restaurantId = selectedRestaurantId,
            name = name.trim(),
            description = description.trim(),
            price = price,
            category = category.trim(),
            imageUrl = imageUrl.trim().ifBlank { existingItem?.imageUrl.orEmpty() },
            isAvailable = existingItem?.isAvailable ?: true,
            isSpiceLevelEnabled = isSpiceLevelEnabled,
            extraOptions = extraOptions,
            removableIngredients = removableIngredientOptions.map { it.name },
            removableIngredientOptions = removableIngredientOptions,
            spiceLevels = spiceOptions.map { it.name },
            spiceLevelOptions = spiceOptions
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

    fun saveRestaurantSettings(
        name: String,
        cuisineType: String,
        deliveryTime: String,
        imageUrl: String,
        isOpen: Boolean
    ) {
        val restaurant = _uiState.value.selectedRestaurant

        if (restaurant == null) {
            _uiState.update { it.copy(errorMessage = "Selectionnez un restaurant.") }
            return
        }

        if (!canManageRestaurant(restaurant.id)) {
            _uiState.update {
                it.copy(errorMessage = "Vous ne pouvez pas modifier ce restaurant.")
            }
            return
        }

        if (
            name.trim().isBlank() ||
            cuisineType.trim().isBlank() ||
            deliveryTime.trim().isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Completez le nom, la cuisine et le temps de livraison.")
            }
            return
        }

        val updatedRestaurant = restaurant.copy(
            name = name.trim(),
            cuisineType = cuisineType.trim(),
            deliveryTime = deliveryTime.trim(),
            imageUrl = imageUrl.trim(),
            isOpen = isOpen
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            restaurantRepository.updateRestaurant(updatedRestaurant)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Restaurant mis a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de modifier le restaurant."
                        )
                    }
                }
        }
    }

    fun saveDriver(
        driverId: String,
        fullName: String,
        phone: String,
        vehicleType: String,
        ratingText: String,
        isAvailable: Boolean
    ) {
        if (!UserRoles.hasGlobalRestaurantAccess(_uiState.value.role)) {
            _uiState.update {
                it.copy(errorMessage = "Seul l'admin global peut gerer les livreurs.")
            }
            return
        }

        val rating = ratingText.replace(",", ".")
            .toDoubleOrNull()
            ?.coerceIn(0.0, 5.0)
            ?: 0.0

        if (fullName.trim().isBlank() || phone.trim().isBlank() || vehicleType.trim().isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Completez le nom, le telephone et le vehicule.")
            }
            return
        }

        val driver = Driver(
            id = driverId,
            fullName = fullName.trim(),
            phone = phone.trim(),
            rating = rating,
            vehicleType = vehicleType.trim(),
            isAvailable = isAvailable
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            val result = if (driverId.isBlank()) {
                driverRepository.createDriver(driver).map { Unit }
            } else {
                driverRepository.updateDriver(driver)
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Livreur enregistre."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible d'enregistrer le livreur."
                        )
                    }
                }
        }
    }

    fun updateDriverAvailability(
        driver: Driver,
        isAvailable: Boolean
    ) {
        saveDriver(
            driverId = driver.id,
            fullName = driver.fullName,
            phone = driver.phone,
            vehicleType = driver.vehicleType,
            ratingText = driver.rating.toString(),
            isAvailable = isAvailable
        )
    }

    fun saveUserAccess(
        user: User,
        role: String,
        managedRestaurantIds: List<String>,
        driverId: String
    ) {
        if (!UserRoles.hasGlobalRestaurantAccess(_uiState.value.role)) {
            _uiState.update {
                it.copy(errorMessage = "Seul l'admin global peut gerer les roles.")
            }
            return
        }

        val cleanRole = role.takeIf { it in editableRoles() } ?: UserRoles.CUSTOMER
        val cleanRestaurantIds = if (cleanRole == UserRoles.PARTNER) {
            managedRestaurantIds.cleanRestaurantIds()
        } else {
            emptyList()
        }
        val cleanDriverId = if (cleanRole == UserRoles.DRIVER) {
            driverId.trim()
        } else {
            ""
        }

        if (cleanRole == UserRoles.PARTNER && cleanRestaurantIds.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Assignez au moins un restaurant pour ce role.")
            }
            return
        }

        if (cleanRole == UserRoles.DRIVER && cleanDriverId.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Liez ce compte a une fiche livreur.")
            }
            return
        }

        val updatedUser = user.copy(
            role = cleanRole,
            managedRestaurantIds = cleanRestaurantIds,
            driverId = cleanDriverId
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null, errorMessage = null) }

            userRepository.updateUserAccess(updatedUser)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = "Role utilisateur mis a jour."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Impossible de modifier ce role."
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
                val managedRestaurantIds = user?.managedRestaurantIds.orEmpty().cleanRestaurantIds()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = role,
                        managedRestaurantIds = managedRestaurantIds
                    )
                }
                syncUserManagementForRole(role)
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

    private fun observeDrivers() {
        viewModelScope.launch {
            driverRepository.observeDrivers().collect { drivers ->
                _uiState.update { it.copy(drivers = drivers) }
            }
        }
    }

    private fun observeUsers() {
        usersJob?.cancel()
        usersJob = viewModelScope.launch {
            userRepository.observeUsers().collect { users ->
                _uiState.update { it.copy(users = users) }
            }
        }
    }

    private fun syncUserManagementForRole(role: String) {
        if (UserRoles.hasGlobalRestaurantAccess(role)) {
            if (usersJob == null) {
                observeUsers()
            }
            return
        }

        usersJob?.cancel()
        usersJob = null
        _uiState.update { it.copy(users = emptyList()) }
    }

    private fun refreshVisibleRestaurants() {
        val state = _uiState.value
        val visibleRestaurants = allRestaurants.visibleFor(
            role = state.role,
            managedRestaurantIds = state.managedRestaurantIds
        )
        val previousSelection = state.selectedRestaurantId
        val selectedRestaurantId = previousSelection
            .takeIf { restaurantId ->
                visibleRestaurants.any { it.id == restaurantId }
            }
            .orEmpty()
        val hasAuthorizedRole = UserRoles.canUsePartnerDashboard(state.role)
        val isAuthorized = hasAuthorizedRole &&
                (UserRoles.hasGlobalRestaurantAccess(state.role) || visibleRestaurants.isNotEmpty())
        val selectionChanged = previousSelection != selectedRestaurantId

        _uiState.update {
            it.copy(
                isAuthorized = isAuthorized,
                restaurants = visibleRestaurants,
                selectedRestaurantId = selectedRestaurantId,
                orders = if (selectionChanged) emptyList() else it.orders,
                menuItems = if (selectionChanged) emptyList() else it.menuItems,
                errorMessage = accessErrorMessage(state.role, visibleRestaurants)
            )
        }

        if (selectedRestaurantId.isBlank()) {
            ordersJob?.cancel()
            menuJob?.cancel()
            return
        }

        if (selectionChanged) {
            observeSelectedRestaurantData(selectedRestaurantId)
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

    private fun canManageRestaurant(restaurantId: String): Boolean {
        val state = _uiState.value
        return UserRoles.canUsePartnerDashboard(state.role) &&
                (UserRoles.hasGlobalRestaurantAccess(state.role) ||
                        restaurantId in state.managedRestaurantIds)
    }
}

private fun String.toPartnerPriceOrNull(): Double? {
    return replace(",", ".").toDoubleOrNull()
}

private fun List<Restaurant>.visibleFor(
    role: String,
    managedRestaurantIds: List<String>
): List<Restaurant> {
    if (UserRoles.hasGlobalRestaurantAccess(role)) return this

    val allowedIds = managedRestaurantIds.toSet()
    return filter { it.id in allowedIds }
}

private fun List<String>.cleanRestaurantIds(): List<String> {
    return map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun accessErrorMessage(
    role: String,
    visibleRestaurants: List<Restaurant>
): String? {
    return when {
        !UserRoles.canUsePartnerDashboard(role) ->
            "Votre compte n'a pas le role partenaire."

        !UserRoles.hasGlobalRestaurantAccess(role) && visibleRestaurants.isEmpty() ->
            "Aucun restaurant n'est assigne a votre compte."

        else -> null
    }
}

fun editableRoles(): List<String> {
    return listOf(
        UserRoles.CUSTOMER,
        UserRoles.PARTNER,
        UserRoles.DRIVER,
        UserRoles.ADMIN
    )
}

private fun defaultSpiceLevelOptions(): List<CustomizationOption> {
    return listOf(
        CustomizationOption(
            id = "mild",
            name = "Doux",
            description = "Leger et accessible."
        ),
        CustomizationOption(
            id = "medium",
            name = "Normal",
            description = "Equilibre, avec un peu de chaleur."
        ),
        CustomizationOption(
            id = "spicy",
            name = "Piquant",
            description = "Releve pour les amateurs d'epices."
        ),
        CustomizationOption(
            id = "extra-spicy",
            name = "Tres piquant",
            description = "Fort, uniquement si le client aime vraiment le piquant."
        )
    )
}
