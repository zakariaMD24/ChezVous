package com.example.chezvous.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.repository.AdminRepository
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val adminName: String = "",
    val totalUsers: Int = 0,
    val totalOrders: Int = 0,
    val totalRestaurants: Int = 0,
    val todayOrders: Int = 0,
    val ordersByDay: List<Pair<String, Int>> = emptyList(),
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)

class AdminDashboardViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState

    init {
        loadAdminName()
        loadStats()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadStats()
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }

    private fun loadAdminName() {
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _uiState.update { it.copy(adminName = user?.fullName.orEmpty()) }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val usersDeferred = async { adminRepository.getUserCount() }
            val ordersDeferred = async { adminRepository.getOrderCount() }
            val restaurantsDeferred = async { adminRepository.getRestaurantCount() }
            val todayDeferred = async { adminRepository.getTodayOrderCount() }
            val chartDeferred = async { adminRepository.getOrdersLast10Days() }

            val usersResult = usersDeferred.await()
            val ordersResult = ordersDeferred.await()
            val restaurantsResult = restaurantsDeferred.await()
            val todayResult = todayDeferred.await()
            val chartResult = chartDeferred.await()

            val anyFailed = listOf(usersResult, ordersResult, restaurantsResult, todayResult)
                .any { it.isFailure }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalUsers = usersResult.getOrDefault(0),
                    totalOrders = ordersResult.getOrDefault(0),
                    totalRestaurants = restaurantsResult.getOrDefault(0),
                    todayOrders = todayResult.getOrDefault(0),
                    ordersByDay = chartResult.getOrDefault(emptyList()),
                    errorMessage = if (anyFailed) "Impossible de charger certaines statistiques." else null
                )
            }
        }
    }
}
