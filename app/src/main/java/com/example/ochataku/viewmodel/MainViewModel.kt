package com.example.ochataku.viewmodel

// MainViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.model.Auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val authManager: AuthManager) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        checkLoginState()
    }

    private fun checkLoginState() {

        viewModelScope.launch {
            delay(500)
            val isLoggedIn = authManager.isLoggedIn()
            _uiState.value = if (isLoggedIn) {
                UiState.LoggedIn
            } else {
                UiState.LoggedOut
            }
        }
    }

    fun handleLoginSuccess() {
        authManager.saveLoginState(true)
        _uiState.value = UiState.LoggedIn
    }

    fun handleLogout() {
        authManager.saveLoginState(false)
        _uiState.value = UiState.LoggedOut
    }

    fun getAuth(): Auth {
        return authManager.getAuth()
    }

    sealed class UiState {
        object Loading : UiState()
        object LoggedIn : UiState()
        object LoggedOut : UiState()
    }
}