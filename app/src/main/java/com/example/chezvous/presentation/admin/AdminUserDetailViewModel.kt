package com.example.chezvous.presentation.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.User
import com.example.chezvous.data.repository.AdminUserRepository
import com.example.chezvous.data.repository.UserOrderStats
import com.example.chezvous.data.repository.UserOrderSummary
import com.example.chezvous.navigation.AdminRoutes
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val orderStats: UserOrderStats = UserOrderStats(),
    val recentOrders: List<UserOrderSummary> = emptyList(),
    val errorMessage: String? = null
)

class AdminUserDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle[AdminRoutes.USER_ID_ARG])
    private val repository = AdminUserRepository()

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun refresh() {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = AdminUserDetailUiState(isLoading = true)
            try {
                coroutineScope {
                    val userDeferred = async { repository.getUser(userId) }
                    val statsDeferred = async { repository.getUserOrderStats(userId) }
                    val ordersDeferred = async { repository.getUserRecentOrders(userId) }

                    _uiState.value = AdminUserDetailUiState(
                        isLoading = false,
                        user = userDeferred.await().getOrNull(),
                        orderStats = statsDeferred.await().getOrDefault(UserOrderStats()),
                        recentOrders = ordersDeferred.await().getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AdminUserDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Erreur lors du chargement"
                )
            }
        }
    }
}
