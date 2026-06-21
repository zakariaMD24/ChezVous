package com.example.chezvous.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.AppNotification
import com.example.chezvous.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminNotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList()
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

class AdminNotificationsViewModel : ViewModel() {

    private val notificationRepository = NotificationRepository()

    private val _uiState = MutableStateFlow(AdminNotificationsUiState())
    val uiState: StateFlow<AdminNotificationsUiState> = _uiState

    init {
        viewModelScope.launch {
            notificationRepository.observeNotifications().collect { notifications ->
                _uiState.update { it.copy(isLoading = false, notifications = notifications) }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value.notifications
                .filter { !it.isRead }
                .forEach { notificationRepository.markAsRead(it.id) }
        }
    }
}
