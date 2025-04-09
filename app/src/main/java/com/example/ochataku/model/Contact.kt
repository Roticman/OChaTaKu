package com.example.ochataku.model

data class Contact(
    val userId: Long,
    val username: String,
    val phoneNumber: String,
    val avatarUrl: String? = null // 头像 URL，可能为空
)
