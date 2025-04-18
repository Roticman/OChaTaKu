package com.example.ochataku.manager

import android.content.Context
import androidx.core.content.edit
import com.example.ochataku.data.local.user.UserEntity

class AuthManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // ✅ 保存用户登录状态
    fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit { putBoolean("is_logged_in", isLoggedIn) }
    }

    fun isLoggedIn(): Boolean {
        return cachedLoggedIn ?: prefs.getBoolean("is_logged_in", false).also {
            cachedLoggedIn = it
        }
    }

    companion object {
        @Volatile private var cachedLoggedIn: Boolean? = null
    }

    // ✅ 保存用户信息（根据 UserEntity）
    fun saveUser(user: UserEntity) {
        prefs.edit {
            putLong("user_id", user.userId)
            putString("username", user.username)
            putString("nickname", user.nickname)
            putString("email", user.email)
            putString("avatar", user.avatar)
            putString("phone", user.phone)
            putInt("gender", user.gender ?: 0)
            putString("region", user.region)
            putString("signature", user.signature)
            putString("birth_date", user.birthDate)
        }
    }

    // ✅ 获取用户信息
    fun getUser(): UserEntity {
        return UserEntity(
            userId = prefs.getLong("user_id", -1L),
            username = prefs.getString("username", null),
            password = "", // 密码不存本地
            nickname = prefs.getString("nickname", "") ?: "",
            avatar = prefs.getString("avatar", null),
            phone = prefs.getString("phone", null),
            email = prefs.getString("email", null),
            gender = prefs.getInt("gender", 0),
            region = prefs.getString("region", null),
            signature = prefs.getString("signature", null),
            birthDate = prefs.getString("birth_date", null)
        )
    }

    fun getUserId(): Long {
        return prefs.getLong("user_id", -1)
    }

    // ✅ 清除所有
    fun clearAuth() {
        prefs.edit { clear() }
        cachedLoggedIn = false
    }
}