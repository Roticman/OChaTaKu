package com.example.ochataku.manager

import android.content.Context
import androidx.core.content.edit
import com.example.ochataku.model.Auth

class AuthManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // ✅ 保存用户登录状态
    fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit { putBoolean("is_logged_in", isLoggedIn) }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    // ✅ 保存用户信息
    fun saveAuth(id: Long, authName: String, email: String, avatarUrl: String?, bio: String?) {
        prefs.edit {
            putLong("auth_id", id)
            putString("auth_name", authName)
            putString("email", email)
            putString("avatar_url", avatarUrl)
            putString("bio", bio)
        }
    }

    // ✅ 获取用户信息
    fun getAuth(): Auth {
        val id = prefs.getLong("auth_id", -1)
        val authName = prefs.getString("auth_name", null)
        val email = prefs.getString("email", null)
        val avatarUrl = prefs.getString("avatar_url", null)
        val bio = prefs.getString("bio", null)

        return if (id != -1L && authName != null && email != null) {
            Auth(id, authName, email, avatarUrl, bio)
        } else {
            Auth(-1, "未知用户", "unknown@example.com", null, null) // 没有用户信息
        }
    }

    fun getAuthId(): Long {
        return prefs.getLong("auth_id", -1) // 默认值 -1 表示没有存储
    }

    // ✅ 清除用户信息（登出时调用）
    fun clearAuth() {
        prefs.edit { clear() }
    }
}


