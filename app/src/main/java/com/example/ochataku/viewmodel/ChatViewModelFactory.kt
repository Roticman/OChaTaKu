package com.example.ochataku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ochataku.data.local.privatemessage.PrivateMessageDao

//ViewModelProviders.of() 方法内部创建了默认的 ViewModelProvider.Factory，支持无参 ViewModel 的实例化。
// 因此，当构造方法包含参数时，ViewModelProvider.Factory 无法正确实例化 ViewModel，因为它仅调用主构造方法，导致创建失败。
class ChatViewModelFactory(
    private val messageDao: PrivateMessageDao,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(messageDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
