package com.example.ochataku.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.privatemessage.PrivateMessageDao
import com.example.ochataku.data.local.privatemessage.PrivateMessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val messageDao: PrivateMessageDao, private val userId: Long) :
    ViewModel() {

    private val _messages = MutableStateFlow<List<PrivateMessageEntity>>(emptyList())
    val messages: StateFlow<List<PrivateMessageEntity>> = _messages

    fun loadMessages(receiverId: Long) {
        viewModelScope.launch {
            messageDao.getChatHistory(userId, receiverId)  // 监听数据库变化
                .collect { messages ->
                    _messages.value = messages  // 触发 UI 更新
                }
        }
    }

    fun sendMessage(receiverId: Long, content: String) {
        viewModelScope.launch {
            val message = PrivateMessageEntity(
                senderId = userId,
                receiverId = receiverId,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            messageDao.insertMessage(message)
        }
    }

    fun markAsRead(messageId: Long) {
        viewModelScope.launch {
            messageDao.markMessageAsRead(messageId)
        }
    }
}



