package com.example.chezvous.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.RestaurantRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val ALL_CUISINES = "__all__"
const val MIN_RATING_FILTER = 0.0
const val MAX_RATING_FILTER = 5.0
const val TOP_RATING_FILTER = 4.5
const val DEFAULT_MAX_DELIVERY_MINUTES = 60
const val FAST_DELIVERY_MINUTES = 30
const val MIN_DELIVERY_FILTER = 20
const val MAX_DELIVERY_FILTER = 60
const val DEFAULT_MAX_MINIMUM_ORDER = 100.0
const val MINIMUM_ORDER_FILTER_MIN = 20.0
const val MINIMUM_ORDER_FILTER_MAX = 100.0

enum class RestaurantSortOption {
    RECOMMENDED,
    TOP_RATED,
    FASTEST_DELIVERY,
    LOWEST_MINIMUM_ORDER,
    NAME_A_Z
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val restaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val cuisineTypes: List<String> = listOf(ALL_CUISINES),
    val selectedCuisine: String = ALL_CUISINES,
    val minimumRating: Double = MIN_RATING_FILTER,
    val maxDeliveryMinutes: Int = DEFAULT_MAX_DELIVERY_MINUTES,
    val maxMinimumOrder: Double = DEFAULT_MAX_MINIMUM_ORDER,
    val onlyOpen: Boolean = false,
    val sortOption: RestaurantSortOption = RestaurantSortOption.RECOMMENDED,
    val cartItemCount: Int = 0,
    val showPartnerDashboard: Boolean = false,
    val isAdminUser: Boolean = false,
    val isRestaurantAdmin: Boolean = false
) {
    val activeFilterCount: Int
        get() = listOf(
            selectedCuisine != ALL_CUISINES,
            minimumRating > MIN_RATING_FILTER,
            maxDeliveryMinutes < DEFAULT_MAX_DELIVERY_MINUTES,
            maxMinimumOrder < DEFAULT_MAX_MINIMUM_ORDER,
            onlyOpen
        ).count { it }
}

class HomeViewModel : ViewModel() {

    private val restaurantRepository = RestaurantRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private var allRestaurants = restaurantRepository.getRestaurants()
    private var allMenuItems = restaurantRepository.getAllMenuItems()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            restaurants = allRestaurants,
            cuisineTypes = cuisineTypesFor(allRestaurants),
            isLoading = allRestaurants.isEmpty()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            restaurantRepository.seedDemoDataIfEmpty()
        }

        viewModelScope.launch {
            restaurantRepository.observeRestaurants().collect { restaurants ->
                allRestaurants = restaurants
                applyFiltersAndSort()
            }
        }

        viewModelScope.launch {
            restaurantRepository.observeAllMenuItems().collect { menuItems ->
                allMenuItems = menuItems
                applyFiltersAndSort()
            }
        }

        viewModelScope.launch {
            CartRepository.cartItems.collect { cartItems ->
                _uiState.update {
                    it.copy(cartItemCount = cartItems.sumOf { item -> item.quantity })
                }
            }
        }

        authRepository.currentUserId()?.let { userId ->
            viewModelScope.launch {
                userRepository.observeUser(userId).collect { user ->
                    _uiState.update {
                        it.copy(
                            showPartnerDashboard = user?.role.isPartnerRole(),
                            isAdminUser = user?.role == UserRoles.ADMIN,
                            isRestaurantAdmin = user?.role == UserRoles.ADMIN_RESTAURANT
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun onCuisineSelected(cuisine: String) {
        _uiState.update { it.copy(selectedCuisine = cuisine) }
        applyFiltersAndSort()
    }

    fun onMinimumRatingChange(value: Double) {
        _uiState.update {
            it.copy(minimumRating = value.coerceIn(MIN_RATING_FILTER, MAX_RATING_FILTER))
        }
        applyFiltersAndSort()
    }

    fun onMaxDeliveryMinutesChange(value: Int) {
        _uiState.update {
            it.copy(maxDeliveryMinutes = value.coerceIn(MIN_DELIVERY_FILTER, MAX_DELIVERY_FILTER))
        }
        applyFiltersAndSort()
    }

    fun onMaxMinimumOrderChange(value: Double) {
        _uiState.update {
            it.copy(
                maxMinimumOrder = value.coerceIn(
                    MINIMUM_ORDER_FILTER_MIN,
                    MINIMUM_ORDER_FILTER_MAX
                )
            )
        }
        applyFiltersAndSort()
    }

    fun onOnlyOpenChange(value: Boolean) {
        _uiState.update { it.copy(onlyOpen = value) }
        applyFiltersAndSort()
    }

    fun onSortSelected(option: RestaurantSortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    fun toggleFastDelivery() {
        val enabled = _uiState.value.maxDeliveryMinutes <= FAST_DELIVERY_MINUTES
        _uiState.update {
            it.copy(
                maxDeliveryMinutes = if (enabled) {
                    DEFAULT_MAX_DELIVERY_MINUTES
                } else {
                    FAST_DELIVERY_MINUTES
                }
            )
        }
        applyFiltersAndSort()
    }

    fun toggleTopRated() {
        val enabled = _uiState.value.minimumRating >= TOP_RATING_FILTER
        _uiState.update {
            it.copy(
                minimumRating = if (enabled) MIN_RATING_FILTER else TOP_RATING_FILTER
            )
        }
        applyFiltersAndSort()
    }

    fun toggleOpenFilter() {
        _uiState.update { it.copy(onlyOpen = !it.onlyOpen) }
        applyFiltersAndSort()
    }

    fun clearCuisineFilter() {
        _uiState.update { it.copy(selectedCuisine = ALL_CUISINES) }
        applyFiltersAndSort()
    }

    fun clearRatingFilter() {
        _uiState.update { it.copy(minimumRating = MIN_RATING_FILTER) }
        applyFiltersAndSort()
    }

    fun clearDeliveryFilter() {
        _uiState.update { it.copy(maxDeliveryMinutes = DEFAULT_MAX_DELIVERY_MINUTES) }
        applyFiltersAndSort()
    }

    fun clearMinimumOrderFilter() {
        _uiState.update { it.copy(maxMinimumOrder = DEFAULT_MAX_MINIMUM_ORDER) }
        applyFiltersAndSort()
    }

    fun clearOpenFilter() {
        _uiState.update { it.copy(onlyOpen = false) }
        applyFiltersAndSort()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCuisine = ALL_CUISINES,
                minimumRating = MIN_RATING_FILTER,
                maxDeliveryMinutes = DEFAULT_MAX_DELIVERY_MINUTES,
                maxMinimumOrder = DEFAULT_MAX_MINIMUM_ORDER,
                onlyOpen = false
            )
        }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val state = _uiState.value
        val cuisineTypes = cuisineTypesFor(allRestaurants)
        val selectedCuisine = state.selectedCuisine.takeIf { it in cuisineTypes } ?: ALL_CUISINES
        val query = state.searchQuery.trim()
        val matchingRestaurantIds = matchingRestaurantIdsFromMenu(query)

        val restaurants = allRestaurants
            .asSequence()
            .filter { restaurant ->
                restaurant.matchesSearch(query, matchingRestaurantIds) &&
                        restaurant.matchesCuisine(selectedCuisine) &&
                        restaurant.rating >= state.minimumRating &&
                        (restaurant.maxDeliveryMinutes() ?: Int.MAX_VALUE) <= state.maxDeliveryMinutes &&
                        restaurant.minimumOrder <= state.maxMinimumOrder &&
                        (!state.onlyOpen || restaurant.isOpen)
            }
            .toList()
            .sortedByOption(state.sortOption)

        _uiState.update {
            it.copy(
                isLoading = false,
                restaurants = restaurants,
                cuisineTypes = cuisineTypes,
                selectedCuisine = selectedCuisine
            )
        }
    }

    private fun matchingRestaurantIdsFromMenu(query: String): Set<String> {
        if (query.isBlank()) return emptySet()

        return allMenuItems
            .asSequence()
            .filter { it.matchesQuery(query) }
            .map { it.restaurantId }
            .toSet()
    }
}

fun Double.asRatingFilterLabel(): String {
    return "${formatOneDecimal()}+"
}

fun Double.asOrderFilterLabel(): String {
    return if (this % 1.0 == 0.0) "${toInt()} DH" else "$this DH"
}

fun Double.roundToHalf(): Double {
    return (this * 2).roundToInt() / 2.0
}

fun Float.roundToNearestFive(): Int {
    return (this / 5f).roundToInt() * 5
}

private fun cuisineTypesFor(restaurants: List<Restaurant>): List<String> {
    return listOf(ALL_CUISINES) +
            restaurants
                .map { it.cuisineType }
                .filter { it.isNotBlank() }
                .distinct()
}

private fun Restaurant.matchesSearch(
    query: String,
    matchingRestaurantIds: Set<String>
): Boolean {
    return query.isBlank() || matchesQuery(query) || id in matchingRestaurantIds
}

private fun Restaurant.matchesCuisine(cuisine: String): Boolean {
    return cuisine == ALL_CUISINES || cuisineType.equals(cuisine, ignoreCase = true)
}

private fun Restaurant.matchesQuery(query: String): Boolean {
    return name.contains(query, ignoreCase = true) ||
            cuisineType.contains(query, ignoreCase = true) ||
            deliveryTime.contains(query, ignoreCase = true)
}

private fun FoodItem.matchesQuery(query: String): Boolean {
    return name.contains(query, ignoreCase = true) ||
            description.contains(query, ignoreCase = true) ||
            category.contains(query, ignoreCase = true)
}

private fun Restaurant.maxDeliveryMinutes(): Int? {
    return Regex("\\d+")
        .findAll(deliveryTime)
        .mapNotNull { it.value.toIntOrNull() }
        .maxOrNull()
}

private fun List<Restaurant>.sortedByOption(option: RestaurantSortOption): List<Restaurant> {
    return when (option) {
        RestaurantSortOption.RECOMMENDED -> this
        RestaurantSortOption.TOP_RATED -> sortedByDescending { it.rating }
        RestaurantSortOption.FASTEST_DELIVERY -> sortedBy { it.maxDeliveryMinutes() ?: Int.MAX_VALUE }
        RestaurantSortOption.LOWEST_MINIMUM_ORDER -> sortedBy { it.minimumOrder }
        RestaurantSortOption.NAME_A_Z -> sortedBy { it.name.lowercase() }
    }
}

private fun Double.formatOneDecimal(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", this)
    }
}

private fun String?.isPartnerRole(): Boolean {
    return this == UserRoles.PARTNER || this == UserRoles.ADMIN
}
