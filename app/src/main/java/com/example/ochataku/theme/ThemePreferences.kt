package com.example.ochataku.theme


import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 创建 DataStore 实例
val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemePreferences {
    private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")

    fun getPrimaryColor(context: Context): Flow<Color> {
        return context.themeDataStore.data.map { preferences ->
            val hex = preferences[PRIMARY_COLOR_KEY] ?: "#6200EE"
            try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                Color(0xFF6200EE) // 默认紫色
            }
        }
    }

    suspend fun savePrimaryColor(context: Context, color: Color) {
        val hex = "#%02X%02X%02X".format(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )

        context.themeDataStore.edit { prefs ->
            prefs[PRIMARY_COLOR_KEY] = hex
        }
    }
}