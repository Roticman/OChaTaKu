package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.ChatDao
import com.example.ochataku.model.ChatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(private val dao: ChatDao, private val userId: Long) : ViewModel() {
    private val _chatList = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatList: StateFlow<List<ChatItem>> = _chatList

    init {
        loadChatList()
    }

    private fun loadChatList() {
        viewModelScope.launch {
            val privateChats = dao.getPrivateChats(userId)
            val groupChats = dao.getGroupChats(userId)

            _chatList.value = (privateChats + groupChats).sortedByDescending { it.lastMessageTime }
        }
    }
}
