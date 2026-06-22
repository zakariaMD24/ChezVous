package com.example.chezvous.presentation.restaurantowner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.RestaurantRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestaurantOwnerMenuUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    // Server-filtered via Firestore whereEqualTo("restaurantId", myRestaurantId)
    val myRestaurantItems: List<FoodItem> = emptyList(),
    val restaurantNames: Map<String, String> = emptyMap(),
    val myRestaurantId: String = "",
    val showMyRestaurantOnly: Boolean = false
) {
    val displayedItems: List<FoodItem>
        get() = if (showMyRestaurantOnly && myRestaurantId.isNotBlank()) {
            allItems.filter { it.restaurantId == myRestaurantId }
        } else {
            allItems
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantOwnerMenuViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val restaurantRepository = RestaurantRepository()

    private val _uiState = MutableStateFlow(RestaurantOwnerMenuUiState())
    val uiState: StateFlow<RestaurantOwnerMenuUiState> = _uiState

    init {
        // Load all items + restaurant name map for the Accueil tab
        viewModelScope.launch {
            combine(
                restaurantRepository.observeAllMenuItems(),
                restaurantRepository.observeRestaurants()
            ) { items, restaurants ->
                Pair(items, restaurants)
            }.collect { (items, restaurants) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allItems = items,
                        restaurantNames = restaurants.associate { r -> r.id to r.name }
                    )
                }
            }
        }

        // Load owner's restaurantId then switch to a Firestore-filtered query for Mes Plats
        authRepository.currentUserId()?.let { userId ->
            viewModelScope.launch {
                userRepository.observeUser(userId)
                    .map { user -> user?.restaurantId.orEmpty().trim() }
                    .distinctUntilChanged()
                    .onEach { restaurantId ->
                        _uiState.update { it.copy(myRestaurantId = restaurantId) }
                    }
                    .flatMapLatest { restaurantId ->
                        if (restaurantId.isNotBlank()) {
                            // RestaurantRepository.observeMenuItems queries Firestore with
                            // .whereEqualTo("restaurantId", restaurantId) — server-side filter
                            restaurantRepository.observeMenuItems(restaurantId)
                        } else {
                            flowOf(emptyList())
                        }
                    }
                    .collect { items ->
                        _uiState.update { it.copy(myRestaurantItems = items) }
                    }
            }
        }
    }

    fun setShowMyRestaurantOnly(value: Boolean) {
        _uiState.update { it.copy(showMyRestaurantOnly = value) }
    }
}
