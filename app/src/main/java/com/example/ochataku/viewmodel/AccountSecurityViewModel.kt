package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSecurityViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog = _showDeleteAccountDialog.asStateFlow()

    fun deleteAccount(onComplete: () -> Unit) {
        authManager.deactivateAccount(
            scope = viewModelScope,
            onSuccess = {
                onComplete()
            },
            onFailure = {
                // 这里可以记录日志或者弹出错误提示
            }
        )
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authManager.clearAuth()
            onComplete()
        }
    }


    fun showDeleteDialog() {
        _showDeleteAccountDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteAccountDialog.value = false
    }


}