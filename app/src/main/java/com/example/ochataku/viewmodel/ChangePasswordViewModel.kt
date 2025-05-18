package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.R
import com.example.ochataku.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordState())
    val uiState = _uiState.asStateFlow()

    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    fun updateNewPassword(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                newPasswordError = null,
                confirmPasswordError = if (password != it.confirmPassword) "密码不匹配" else null
            )
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update {
            it.copy(
                confirmPassword = password,
                confirmPasswordError = if (password != it.newPassword) "密码不匹配" else null
            )
        }
    }

    fun changePassword() {
        val currentState = _uiState.value

        // 验证输入
        val currentError = if (currentState.currentPassword.isEmpty()) "请输入当前密码" else null
        val newError = when {
            currentState.newPassword.isEmpty() -> "请输入新密码"
            currentState.newPassword.length < 8 -> "密码至少8位字符"
            !currentState.newPassword.any { it.isUpperCase() } -> "至少包含一个大写字母"
            !currentState.newPassword.any { it.isDigit() } -> "至少包含一个数字"
            else -> null
        }
        val confirmError = if (currentState.newPassword != currentState.confirmPassword) "密码不匹配" else null

        _uiState.update {
            it.copy(
                currentPasswordError = currentError,
                newPasswordError = newError,
                confirmPasswordError = confirmError,
                isLoading = currentError == null && newError == null && confirmError == null
            )
        }

        if (currentError != null || newError != null || confirmError != null) {
            return
        }

        viewModelScope.launch {
            try {
                authManager.changePassword(
                    currentPassword = currentState.currentPassword,
                    newPassword = currentState.newPassword
                )
                _uiState.update { it.copy(isLoading = false, isSuccess = true, message = "密码修改成功") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "密码修改失败"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}