package com.example.ochataku.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.repository.FriendRequestRepository
import com.example.ochataku.service.FriendRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val repository: FriendRequestRepository
) : ViewModel() {

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests

    fun loadFriendRequests() {
        viewModelScope.launch {
            _friendRequests.value = repository.getFriendRequests()
        }
    }

    fun handleFriendRequest(requestId: Int, action: String, onResult: () -> Unit) {
        viewModelScope.launch {
            repository.handleFriendRequest(requestId, action)
            loadFriendRequests() // 处理完重新加载
            onResult()
        }
    }
}