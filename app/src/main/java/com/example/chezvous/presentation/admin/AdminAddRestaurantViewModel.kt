package com.example.chezvous.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.AdminRestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAddRestaurantUiState(
    val name: String = "",
    val cuisineType: String = "",
    val deliveryTime: String = "",
    val minimumOrder: String = "",
    val imageUrl: String = "",
    val isOpen: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    // per-field validation errors shown after the first save attempt
    val nameError: Boolean = false,
    val cuisineError: Boolean = false,
    val deliveryError: Boolean = false,
    val minimumOrderError: Boolean = false
)

class AdminAddRestaurantViewModel : ViewModel() {

    private val repository = AdminRestaurantRepository()

    private val _uiState = MutableStateFlow(AdminAddRestaurantUiState())
    val uiState: StateFlow<AdminAddRestaurantUiState> = _uiState

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = false) }
    fun onCuisineTypeChange(value: String) = _uiState.update { it.copy(cuisineType = value, cuisineError = false) }
    fun onDeliveryTimeChange(value: String) = _uiState.update { it.copy(deliveryTime = value, deliveryError = false) }
    fun onMinimumOrderChange(value: String) {
        val filtered = value.filter { c -> c.isDigit() || c == '.' || c == ',' }
        _uiState.update { it.copy(minimumOrder = filtered, minimumOrderError = false) }
    }
    fun onImageUrlChange(value: String) = _uiState.update { it.copy(imageUrl = value) }
    fun onIsOpenChange(value: Boolean) = _uiState.update { it.copy(isOpen = value) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun save() {
        val state = _uiState.value

        val nameBlank = state.name.isBlank()
        val cuisineBlank = state.cuisineType.isBlank()
        val deliveryBlank = state.deliveryTime.isBlank()
        val minimumInvalid = state.minimumOrder.isNotBlank() &&
            state.minimumOrder.replace(",", ".").toDoubleOrNull() == null

        if (nameBlank || cuisineBlank || deliveryBlank || minimumInvalid) {
            _uiState.update {
                it.copy(
                    nameError = nameBlank,
                    cuisineError = cuisineBlank,
                    deliveryError = deliveryBlank,
                    minimumOrderError = minimumInvalid
                )
            }
            return
        }

        val restaurant = Restaurant(
            name = state.name.trim(),
            cuisineType = state.cuisineType.trim(),
            rating = 0.0,
            deliveryTime = state.deliveryTime.trim(),
            minimumOrder = state.minimumOrder.replace(",", ".").toDoubleOrNull() ?: 0.0,
            imageUrl = state.imageUrl.trim(),
            isOpen = state.isOpen
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            repository.createRestaurant(restaurant)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message ?: "Erreur lors de l'enregistrement"
                        )
                    }
                }
        }
    }
}
