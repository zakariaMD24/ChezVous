package com.example.chezvous.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.User
import com.example.chezvous.data.repository.AdminUserRepository
import com.example.chezvous.data.repository.UserOrderStats
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUserItem(
    val user: User,
    val orderStats: UserOrderStats
)

sealed interface AdminUsersUiState {
    data object Loading : AdminUsersUiState
    data class Success(val users: List<AdminUserItem>) : AdminUsersUiState
    data class Error(val message: String) : AdminUsersUiState
}

class AdminUsersViewModel : ViewModel() {

    private val repository = AdminUserRepository()

    private val _uiState = MutableStateFlow<AdminUsersUiState>(AdminUsersUiState.Loading)
    val uiState: StateFlow<AdminUsersUiState> = _uiState

    init {
        loadUsers()
    }

    fun refresh() {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            repository.getCustomers()
                .onSuccess { users ->
                    val items = coroutineScope {
                        users.map { user ->
                            async {
                                val stats = repository.getUserOrderStats(user.id)
                                    .getOrDefault(UserOrderStats())
                                AdminUserItem(user = user, orderStats = stats)
                            }
                        }.map { it.await() }
                    }
                    _uiState.value = AdminUsersUiState.Success(
                        items.sortedByDescending { it.orderStats.total }
                    )
                }
                .onFailure { e ->
                    _uiState.value = AdminUsersUiState.Error(
                        e.message ?: "Impossible de charger les utilisateurs"
                    )
                }
        }
    }
}
