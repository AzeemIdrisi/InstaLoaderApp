package com.alphacorp.instaloader.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alphacorp.instaloader.data.model.toDownloadError
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val twoFactorCode: String = "",
    val showTwoFactor: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class LoginViewModel(
    private val sessionRepository: SessionRepository,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(isLoggedIn = sessionRepository.username != null),
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            username = sessionRepository.username.orEmpty(),
            isLoggedIn = sessionRepository.username != null,
        )
    }

    fun onUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onTwoFactorChanged(value: String) {
        _uiState.value = _uiState.value.copy(twoFactorCode = value, errorMessage = null)
    }

    fun login() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter username and password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                sessionRepository.login(
                    username = username,
                    password = password,
                    sessionDir = downloadRepository.sessionDirectory().absolutePath,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    password = "",
                    successMessage = "Logged in as $username",
                    showTwoFactor = false,
                )
            }.onFailure { error ->
                val mapped = error.toDownloadError()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = mapped.userMessage,
                    showTwoFactor = mapped is com.alphacorp.instaloader.data.model.DownloadError.TwoFactorRequired,
                )
            }
        }
    }

    fun submitTwoFactor() {
        val code = _uiState.value.twoFactorCode.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter the 2FA code.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                sessionRepository.submitTwoFactor(
                    code = code,
                    sessionDir = downloadRepository.sessionDirectory().absolutePath,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    twoFactorCode = "",
                    showTwoFactor = false,
                    successMessage = "Two-factor authentication successful",
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.toDownloadError().userMessage,
                )
            }
        }
    }

    fun logout() {
        sessionRepository.logout()
        _uiState.value = LoginUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    class Factory(
        private val sessionRepository: SessionRepository,
        private val downloadRepository: DownloadRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(sessionRepository, downloadRepository) as T
        }
    }
}
