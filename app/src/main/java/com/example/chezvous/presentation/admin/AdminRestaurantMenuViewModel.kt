package com.example.chezvous.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.repository.RestaurantRepository
import com.example.chezvous.navigation.AdminRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class AdminRestaurantMenuUiState(
    val isLoading: Boolean = true,
    val items: List<FoodItem> = emptyList(),
    val errorMessage: String? = null
)

class AdminRestaurantMenuViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val restaurantId: String = checkNotNull(savedStateHandle[AdminRoutes.RESTAURANT_ID_ARG])
    private val repository = RestaurantRepository()

    private val _uiState = MutableStateFlow(AdminRestaurantMenuUiState())
    val uiState: StateFlow<AdminRestaurantMenuUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.observeMenuItems(restaurantId)
                .catch { e ->
                    _uiState.value = AdminRestaurantMenuUiState(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible de charger le menu"
                    )
                }
                .collect { items ->
                    _uiState.value = AdminRestaurantMenuUiState(
                        isLoading = false,
                        items = items
                    )
                }
        }
    }
}
