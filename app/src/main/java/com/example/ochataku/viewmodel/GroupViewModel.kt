package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.group.GroupEntity
import com.example.ochataku.repository.ConversationRepository
import com.example.ochataku.repository.GroupRepository
import com.example.ochataku.model.ContactConvResponse
import com.example.ochataku.model.ContactRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _groupList = MutableStateFlow<List<GroupEntity>>(emptyList())
    val groupList: StateFlow<List<GroupEntity>> = _groupList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadGroups(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = groupRepository.fetchGroups(userId)
            _groupList.value = result ?: emptyList()
            _isLoading.value = false
        }
    }

    suspend fun getOrCreateConversation(request: ContactRequest): ContactConvResponse {
        return conversationRepository.getOrCreateConversation(request)
    }
}
