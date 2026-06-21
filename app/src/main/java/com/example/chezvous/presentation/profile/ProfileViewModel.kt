package com.example.chezvous.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.repository.AuthRepository
import com.example.chezvous.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isLoggedOut: Boolean = false,
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = UserRoles.CUSTOMER,
    val managedRestaurantIds: List<String> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        observeProfile()
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, successMessage = null)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(phone = value, successMessage = null)
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value, successMessage = null)
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.userId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null, successMessage = null)

            val result = userRepository.updateUserProfile(
                userId = state.userId,
                fullName = state.fullName.trim(),
                phone = state.phone.trim(),
                address = state.address.trim()
            )

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Profil enregistre"
                )
            } else {
                _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Impossible d'enregistrer le profil"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = ProfileUiState(isLoading = false, isLoggedOut = true)
    }

    private fun observeProfile() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            _uiState.value = ProfileUiState(isLoading = false, isLoggedOut = true)
            return
        }

        _uiState.value = ProfileUiState(
            userId = userId,
            email = authRepository.currentUserEmail()
        )

        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _uiState.value = if (user != null) {
                    _uiState.value.copy(
                        isLoading = false,
                        userId = user.id,
                        fullName = user.fullName,
                        email = user.email,
                        phone = user.phone,
                        address = user.address,
                        role = user.role,
                        managedRestaurantIds = user.managedRestaurantIds
                    )
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        userId = userId,
                        email = authRepository.currentUserEmail()
                    )
                }
            }
        }
    }
}
