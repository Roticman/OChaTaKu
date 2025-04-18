package com.example.ochataku.model

data class ChatItem(
    val id: Long,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val type: Int // 1=私聊，2=群聊
)
