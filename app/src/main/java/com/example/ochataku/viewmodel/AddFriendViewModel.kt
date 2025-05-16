package com.example.ochataku.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.repository.UserRepository
import com.example.ochataku.service.UserSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<UserSearchResult>>(emptyList())
    val searchResults: StateFlow<List<UserSearchResult>> = _searchResults.asStateFlow()

    fun searchUsers(query: String ,currentUserId: Long) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }

            try {
                val results = userRepository.searchUsers(query)
                val filtered = results.filter { it.userId != currentUserId }

                Log.d("AddFriendViewModel", "搜索成功，过滤后 ${filtered.size} 条")
                _searchResults.value = filtered
            } catch (e: Exception) {
                Log.e("AddFriendViewModel", "搜索失败: ${e.localizedMessage}")
                _searchResults.value = emptyList()
            }
        }
    }


    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun sendFriendRequest(toUserId: Long, onSuccess: () -> Unit, onAlreadyFriend: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.sendFriendRequest(toUserId)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                if (e.code() == 400 && "已经是好友" in errorBody) {
                    onAlreadyFriend()
                } else {
                    onError("添加失败: ${e.message()}")
                }
            } catch (e: Exception) {
                onError("网络错误: ${e.localizedMessage}")
            }
        }
    }

}
