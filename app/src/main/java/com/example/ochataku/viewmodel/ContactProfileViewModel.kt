package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.repository.ContactProfileRepository
import com.example.ochataku.model.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactProfileViewModel @Inject constructor() : ViewModel() {

    private val repository = ContactProfileRepository()

    private val _profileState = MutableStateFlow<ProfileUiState?>(null)
    val profileState: StateFlow<ProfileUiState?> = _profileState

    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            val profile = repository.getContactProfile(userId)
            _profileState.value = profile
        }
    }
}
