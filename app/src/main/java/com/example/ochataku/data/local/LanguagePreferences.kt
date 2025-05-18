package com.example.ochataku.data.local


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object LanguagePreferences {
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    fun getAppLanguage(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY]
        }
    }

    suspend fun setAppLanguage(context: Context, langCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = langCode
        }
    }
}