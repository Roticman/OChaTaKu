package com.example.ochataku.model

// ✅ 定义 Auth 数据类
data class Auth(
    val id: Long,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String?,
)