package com.example.ochataku.model

data class ChatItem(
    val id: Long,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long
)
