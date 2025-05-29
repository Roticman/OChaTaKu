package com.example.ochataku.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.R
import com.example.ochataku.service.ApiService
import com.example.ochataku.model.UpdateProfileRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<String?>(null)
    val uiState: StateFlow<String?> = _uiState


    fun updateProfile(context: Context, request: UpdateProfileRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateProfile(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.value = "${context.getString(R.string.update_failed)}：${response.code()}"
                }
            } catch (e: Exception) {
                _uiState.value = "${context.getString(R.string.call_error)}：${e.message}"
            }
        }
    }
}
