package com.example.ochataku.viewmodel

// MainViewModel.kt
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.ochataku.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val authManager = AuthManager(context)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()


    init {
        refreshLoginStatus()
    }

    fun refreshLoginStatus() {
        val logged = authManager.isLoggedIn()
        _uiState.value = if (logged) UiState.LoggedIn else UiState.LoggedOut
    }


    fun handleLoginSuccess() {
//        authManager.saveAuth(auth.id, auth.username, auth.email, auth.avatarUrl, auth.bio)
        authManager.saveLoginState(true)
        _uiState.value = UiState.LoggedIn
    }

    fun handleLogout() {
        authManager.clearAuth()
        _uiState.value = UiState.LoggedOut
    }

    sealed class UiState {
        object Loading : UiState()
        object LoggedIn : UiState()
        object LoggedOut : UiState()
    }
}