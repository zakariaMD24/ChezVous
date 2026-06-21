package com.example.chezvous.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.AdminRestaurantRepository
import com.example.chezvous.data.repository.RestaurantOrderStats
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminRestaurantItem(
    val restaurant: Restaurant,
    val orderStats: RestaurantOrderStats
)

sealed interface AdminRestaurantsUiState {
    data object Loading : AdminRestaurantsUiState
    data class Success(val restaurants: List<AdminRestaurantItem>) : AdminRestaurantsUiState
    data class Error(val message: String) : AdminRestaurantsUiState
}

class AdminRestaurantsViewModel : ViewModel() {

    private val repository = AdminRestaurantRepository()

    private val _uiState = MutableStateFlow<AdminRestaurantsUiState>(AdminRestaurantsUiState.Loading)
    val uiState: StateFlow<AdminRestaurantsUiState> = _uiState

    init {
        loadRestaurants()
    }

    fun refresh() {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.value = AdminRestaurantsUiState.Loading
            repository.getRestaurants()
                .onSuccess { restaurants ->
                    val items = coroutineScope {
                        restaurants.map { restaurant ->
                            async {
                                val stats = repository.getRestaurantOrderStats(restaurant.id)
                                    .getOrDefault(RestaurantOrderStats())
                                AdminRestaurantItem(restaurant = restaurant, orderStats = stats)
                            }
                        }.map { it.await() }
                    }
                    _uiState.value = AdminRestaurantsUiState.Success(
                        items.sortedByDescending { it.orderStats.total }
                    )
                }
                .onFailure { e ->
                    _uiState.value = AdminRestaurantsUiState.Error(
                        e.message ?: "Impossible de charger les restaurants"
                    )
                }
        }
    }
}
