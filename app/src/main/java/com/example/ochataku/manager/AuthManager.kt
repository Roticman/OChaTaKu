package com.example.ochataku.manager

import android.content.Context
import androidx.core.content.edit
import com.example.ochataku.data.local.user.UserEntity

class AuthManager(private val context: Context) {
    companion object {
        private const val NAME = "auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // 同步写，保证立刻落盘
    fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        }
    }

    // 直接从磁盘读
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // ✅ 保存用户信息（根据 UserEntity）
    fun saveUser(user: UserEntity) {
        prefs.edit(commit = true) {
            putLong("user_id", user.userId)
            putString("username", user.username)
            putString("avatar", user.avatar)
            putString("phone", user.phone)
            putString("email", user.email)
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
        prefs.edit(commit = true) {
            clear()
        }
    }
}