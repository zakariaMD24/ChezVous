package com.example.chezvous.presentation.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.CartActionResult
import com.example.chezvous.data.repository.CartRepository
import com.example.chezvous.data.repository.RestaurantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ALL_CATEGORIES = "Tous"
const val DEFAULT_MAX_MENU_PRICE = 120.0
const val MIN_MENU_PRICE_FILTER = 20.0
const val MAX_MENU_PRICE_FILTER = 120.0

enum class MenuAvailabilityFilter(val label: String) {
    ALL("Tous les plats"),
    AVAILABLE("Disponible"),
    UNAVAILABLE("Indisponible")
}

enum class MenuSortOption(val label: String) {
    DEFAULT("Recommande"),
    PRICE_LOW_HIGH("Prix croissant"),
    PRICE_HIGH_LOW("Prix decroissant"),
    NAME_A_Z("Nom A-Z"),
    AVAILABLE_FIRST("Disponible d'abord")
}

data class RestaurantDetailsUiState(
    val isLoading: Boolean = true,
    val restaurant: Restaurant? = null,
    val menuItems: List<FoodItem> = emptyList(),
    val categories: List<String> = listOf(ALL_CATEGORIES),
    val selectedCategory: String = ALL_CATEGORIES,
    val availabilityFilter: MenuAvailabilityFilter = MenuAvailabilityFilter.ALL,
    val maxPrice: Double = DEFAULT_MAX_MENU_PRICE,
    val sortOption: MenuSortOption = MenuSortOption.DEFAULT,
    val searchQuery: String = "",
    val cartItemCount: Int = 0,
    val cartMessage: String? = null,
    val errorMessage: String? = null
) {
    val activeFilterCount: Int
        get() = listOf(
            selectedCategory != ALL_CATEGORIES,
            availabilityFilter != MenuAvailabilityFilter.ALL,
            maxPrice < DEFAULT_MAX_MENU_PRICE
        ).count { it }
}

class RestaurantDetailsViewModel : ViewModel() {

    private val repository = RestaurantRepository()
    private var currentRestaurantId = ""
    private var allMenuItems = emptyList<FoodItem>()
    private var restaurantJob: Job? = null
    private var menuJob: Job? = null

    private val _uiState = MutableStateFlow(RestaurantDetailsUiState())
    val uiState: StateFlow<RestaurantDetailsUiState> = _uiState

    init {
        viewModelScope.launch {
            CartRepository.cartItems.collect { cartItems ->
                _uiState.update {
                    it.copy(cartItemCount = cartItems.sumOf { item -> item.quantity })
                }
            }
        }
    }

    fun loadRestaurant(restaurantId: String) {
        if (restaurantId.isBlank() || restaurantId == currentRestaurantId) return

        currentRestaurantId = restaurantId
        allMenuItems = repository.getMenuItems(restaurantId)
        _uiState.value = RestaurantDetailsUiState(
            isLoading = true,
            restaurant = repository.getRestaurant(restaurantId)
        )

        restaurantJob?.cancel()
        menuJob?.cancel()

        restaurantJob = viewModelScope.launch {
            repository.observeRestaurants().collect { restaurants ->
                _uiState.update {
                    it.copy(
                        restaurant = restaurants.firstOrNull { restaurant ->
                            restaurant.id == restaurantId
                        } ?: it.restaurant,
                        errorMessage = null
                    )
                }
            }
        }

        menuJob = viewModelScope.launch {
            repository.observeMenuItems(restaurantId).collect { menuItems ->
                allMenuItems = menuItems
                applyMenuFiltersAndSort()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyMenuFiltersAndSort()
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyMenuFiltersAndSort()
    }

    fun onAvailabilityFilterSelected(filter: MenuAvailabilityFilter) {
        _uiState.update { it.copy(availabilityFilter = filter) }
        applyMenuFiltersAndSort()
    }

    fun onMaxPriceChange(value: Double) {
        _uiState.update {
            it.copy(maxPrice = value.coerceIn(MIN_MENU_PRICE_FILTER, MAX_MENU_PRICE_FILTER))
        }
        applyMenuFiltersAndSort()
    }

    fun onSortSelected(option: MenuSortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyMenuFiltersAndSort()
    }

    fun addToCart(foodItem: FoodItem) {
        val restaurant = _uiState.value.restaurant
        if (restaurant == null) {
            _uiState.update { it.copy(cartMessage = "Restaurant introuvable pour ce plat.") }
            return
        }

        val result = CartRepository.addItem(foodItem, restaurant)
        _uiState.update {
            it.copy(
                cartMessage = when (result) {
                    is CartActionResult.Success -> result.message
                    is CartActionResult.Error -> result.message
                }
            )
        }
    }

    fun clearCartMessage() {
        _uiState.update { it.copy(cartMessage = null) }
    }

    fun clearCategoryFilter() {
        _uiState.update { it.copy(selectedCategory = ALL_CATEGORIES) }
        applyMenuFiltersAndSort()
    }

    fun clearAvailabilityFilter() {
        _uiState.update { it.copy(availabilityFilter = MenuAvailabilityFilter.ALL) }
        applyMenuFiltersAndSort()
    }

    fun clearPriceFilter() {
        _uiState.update { it.copy(maxPrice = DEFAULT_MAX_MENU_PRICE) }
        applyMenuFiltersAndSort()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = ALL_CATEGORIES,
                availabilityFilter = MenuAvailabilityFilter.ALL,
                maxPrice = DEFAULT_MAX_MENU_PRICE
            )
        }
        applyMenuFiltersAndSort()
    }

    private fun applyMenuFiltersAndSort() {
        val state = _uiState.value
        val categories = categoriesFor(allMenuItems)
        val selectedCategory = if (state.selectedCategory in categories) {
            state.selectedCategory
        } else {
            ALL_CATEGORIES
        }
        val query = state.searchQuery.trim()

        val filteredMenuItems = allMenuItems.filter { item ->
            val matchesCategory = selectedCategory == ALL_CATEGORIES ||
                    item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = query.isBlank() || item.matchesQuery(query)
            val matchesAvailability = when (state.availabilityFilter) {
                MenuAvailabilityFilter.ALL -> true
                MenuAvailabilityFilter.AVAILABLE -> item.isAvailable
                MenuAvailabilityFilter.UNAVAILABLE -> !item.isAvailable
            }
            val matchesPrice = item.price <= state.maxPrice

            matchesCategory &&
                    matchesSearch &&
                    matchesAvailability &&
                    matchesPrice
        }.sortedWith(state.sortOption.comparator())

        _uiState.update {
            it.copy(
                isLoading = false,
                menuItems = filteredMenuItems,
                categories = categories,
                selectedCategory = selectedCategory,
                errorMessage = null
            )
        }
    }
}

fun Double.asMenuPriceFilterLabel(): String {
    return if (this % 1.0 == 0.0) {
        "${toInt()} DH"
    } else {
        "$this DH"
    }
}

fun Float.roundMenuPriceToNearestFive(): Double {
    return ((this / 5f).roundToInt() * 5).toDouble()
}

private fun categoriesFor(menuItems: List<FoodItem>): List<String> {
    return listOf(ALL_CATEGORIES) +
            menuItems.map { it.category }
                .filter { it.isNotBlank() }
                .distinct()
}

private fun FoodItem.matchesQuery(query: String): Boolean {
    return name.contains(query, ignoreCase = true) ||
            description.contains(query, ignoreCase = true) ||
            category.contains(query, ignoreCase = true)
}

private fun MenuSortOption.comparator(): Comparator<FoodItem> {
    return when (this) {
        MenuSortOption.DEFAULT -> compareBy { 0 }
        MenuSortOption.PRICE_LOW_HIGH -> compareBy { it.price }
        MenuSortOption.PRICE_HIGH_LOW -> compareByDescending { it.price }
        MenuSortOption.NAME_A_Z -> compareBy { it.name.lowercase() }
        MenuSortOption.AVAILABLE_FIRST -> compareByDescending { it.isAvailable }
    }
}
