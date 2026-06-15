package com.example.chezvous.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null
)

class OrdersViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState

    init {
        val userId = authRepository.currentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.value = OrdersUiState(
                isLoading = false,
                errorMessage = "Connectez-vous pour voir vos commandes."
            )
        } else {
            viewModelScope.launch {
                orderRepository.observeUserOrders(userId).collect { orders ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = orders,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}
