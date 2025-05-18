package com.example.ochataku.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _primaryColor = MutableStateFlow(Color(0xFF6200EE))
    val primaryColor: StateFlow<Color> = _primaryColor

    init {
        viewModelScope.launch {
            ThemePreferences.getPrimaryColor(context).collect {
                _primaryColor.value = it
            }
        }
    }

    fun updatePrimaryColor(color: Color) {
        viewModelScope.launch {
            _primaryColor.emit(color)
            ThemePreferences.savePrimaryColor(context, color)
        }
    }
}
