package com.example.ochataku.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.repository.FriendRequestRepository
import com.example.ochataku.model.FriendRequestDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val repository: FriendRequestRepository
) : ViewModel() {

    private val _friendRequests = MutableStateFlow<List<FriendRequestDisplay>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequestDisplay>> = _friendRequests

    fun loadFriendRequests() {
        viewModelScope.launch {
            _friendRequests.value = repository.getFriendRequestsWithProfile()
        }
    }

    fun handleFriendRequest(
        requestId: Int,
        action: String,
        onResult: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.handleFriendRequest(requestId, action)
                loadFriendRequests() // 重新加载请求列表
                onResult()
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = errorBody ?: "处理请求失败：${e.message()}"
                onError(message)
            } catch (e: Exception) {
                onError("网络错误：${e.localizedMessage}")
            }
        }
    }

}