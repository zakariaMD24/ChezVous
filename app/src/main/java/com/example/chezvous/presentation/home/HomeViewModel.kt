package com.example.chezvous.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ALL_CUISINES = "Tous"
const val MIN_RATING_FILTER = 0.0
const val MAX_RATING_FILTER = 5.0
const val DEFAULT_MAX_DELIVERY_MINUTES = 60
const val MIN_DELIVERY_FILTER = 20
const val MAX_DELIVERY_FILTER = 60
const val DEFAULT_MAX_MINIMUM_ORDER = 100.0
const val MINIMUM_ORDER_FILTER_MIN = 20.0
const val MINIMUM_ORDER_FILTER_MAX = 100.0

enum class RestaurantSortOption(val label: String) {
    RECOMMENDED("Recommande"),
    TOP_RATED("Mieux notes"),
    FASTEST_DELIVERY("Livraison rapide"),
    LOWEST_MINIMUM_ORDER("Minimum bas"),
    NAME_A_Z("Nom A-Z")
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
    val cartItemCount: Int = 0
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

    private val repository = RestaurantRepository()
    private var allRestaurants = repository.getRestaurants()
    private var allMenuItems = repository.getAllMenuItems()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            restaurants = allRestaurants,
            cuisineTypes = cuisineTypesFor(allRestaurants),
            isLoading = false
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }

        viewModelScope.launch {
            repository.observeRestaurants().collect { latestRestaurants ->
                allRestaurants = latestRestaurants
                applyFiltersAndSort()
            }
        }

        viewModelScope.launch {
            repository.observeAllMenuItems().collect { latestMenuItems ->
                allMenuItems = latestMenuItems
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
        val selectedCuisine = if (state.selectedCuisine in cuisineTypes) {
            state.selectedCuisine
        } else {
            ALL_CUISINES
        }

        val query = state.searchQuery.trim()
        val restaurantIdsMatchingMenu = if (query.isBlank()) {
            emptySet()
        } else {
            allMenuItems
                .filter { it.matchesQuery(query) }
                .map { it.restaurantId }
                .toSet()
        }

        val filteredRestaurants = allRestaurants.filter { restaurant ->
            val matchesSearch = query.isBlank() ||
                    restaurant.matchesQuery(query) ||
                    restaurant.id in restaurantIdsMatchingMenu
            val matchesCuisine = selectedCuisine == ALL_CUISINES ||
                    restaurant.cuisineType.equals(selectedCuisine, ignoreCase = true)
            val matchesRating = restaurant.rating >= state.minimumRating
            val matchesDelivery = (restaurant.maxDeliveryMinutes() ?: Int.MAX_VALUE) <=
                    state.maxDeliveryMinutes
            val matchesMinimumOrder = restaurant.minimumOrder <= state.maxMinimumOrder
            val matchesOpenState = !state.onlyOpen || restaurant.isOpen

            matchesSearch &&
                    matchesCuisine &&
                    matchesRating &&
                    matchesDelivery &&
                    matchesMinimumOrder &&
                    matchesOpenState
        }.sortedWith(state.sortOption.comparator())

        _uiState.update {
            it.copy(
                isLoading = false,
                restaurants = filteredRestaurants,
                cuisineTypes = cuisineTypes,
                selectedCuisine = selectedCuisine
            )
        }
    }
}

fun Double.asRatingFilterLabel(): String {
    return "${formatOneDecimal()}+"
}

fun Double.asOrderFilterLabel(): String {
    return if (this % 1.0 == 0.0) {
        "${toInt()} DH"
    } else {
        "$this DH"
    }
}

fun Double.roundToHalf(): Double {
    return (this * 2).roundToInt() / 2.0
}

fun Float.roundToNearestFive(): Int {
    return (this / 5f).roundToInt() * 5
}

private fun cuisineTypesFor(restaurants: List<Restaurant>): List<String> {
    return listOf(ALL_CUISINES) +
            restaurants.map { it.cuisineType }
                .filter { it.isNotBlank() }
                .distinct()
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

private fun RestaurantSortOption.comparator(): Comparator<Restaurant> {
    return when (this) {
        RestaurantSortOption.RECOMMENDED -> compareBy { 0 }
        RestaurantSortOption.TOP_RATED -> compareByDescending { it.rating }
        RestaurantSortOption.FASTEST_DELIVERY -> compareBy { it.maxDeliveryMinutes() ?: Int.MAX_VALUE }
        RestaurantSortOption.LOWEST_MINIMUM_ORDER -> compareBy { it.minimumOrder }
        RestaurantSortOption.NAME_A_Z -> compareBy { it.name.lowercase() }
    }
}

private fun Double.formatOneDecimal(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}
