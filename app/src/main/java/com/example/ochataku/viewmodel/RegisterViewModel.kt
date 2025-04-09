package com.example.ochataku.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.AppDatabase
import com.example.ochataku.data.local.user.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    fun registerUser(
        username: String,
        email: String,
        passwordHash: String,
        avatarUri: Uri?
    ) {
        val user = UserEntity(
            username = username,
            email = email,
            passwordHash = passwordHash,
            avatarUrl = avatarUri?.toString()
        )

        viewModelScope.launch(Dispatchers.IO) {
            userDao.insertUser(user)
            _registerSuccess.value = true
        }
    }

    fun resetState() {
        _registerSuccess.value = false
    }
}
