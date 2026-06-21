package com.example.chezvous.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.AdminRestaurantRepository
import com.example.chezvous.data.repository.RestaurantOrderStats
import com.example.chezvous.data.repository.RestaurantOrderSummary
import com.example.chezvous.navigation.AdminRoutes
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminRestaurantDetailUiState(
    val isLoading: Boolean = true,
    val restaurant: Restaurant? = null,
    val orderStats: RestaurantOrderStats = RestaurantOrderStats(),
    val recentOrders: List<RestaurantOrderSummary> = emptyList(),
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val deleteErrorMessage: String? = null
)

class AdminRestaurantDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val restaurantId: String = checkNotNull(savedStateHandle[AdminRoutes.RESTAURANT_ID_ARG])
    private val repository = AdminRestaurantRepository()

    private val _uiState = MutableStateFlow(AdminRestaurantDetailUiState())
    val uiState: StateFlow<AdminRestaurantDetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun refresh() {
        loadDetail()
    }

    fun deleteRestaurant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteErrorMessage = null)
            repository.deleteRestaurant(restaurantId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isDeleting = false, isDeleted = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteErrorMessage = e.message ?: "Erreur lors de la suppression"
                    )
                }
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = AdminRestaurantDetailUiState(isLoading = true)
            try {
                coroutineScope {
                    val restaurantDeferred = async { repository.getRestaurant(restaurantId) }
                    val statsDeferred = async { repository.getRestaurantOrderStats(restaurantId) }
                    val ordersDeferred = async { repository.getRestaurantRecentOrders(restaurantId) }

                    _uiState.value = AdminRestaurantDetailUiState(
                        isLoading = false,
                        restaurant = restaurantDeferred.await().getOrNull(),
                        orderStats = statsDeferred.await().getOrDefault(RestaurantOrderStats()),
                        recentOrders = ordersDeferred.await().getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminRestaurantDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Erreur lors du chargement"
                )
            }
        }
    }
}
