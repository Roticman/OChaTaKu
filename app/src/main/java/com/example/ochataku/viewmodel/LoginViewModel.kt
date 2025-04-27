package com.example.ochataku.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.repository.UserRepository
import com.example.ochataku.service.LoginRequest
import com.example.ochataku.service.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val authManager = AuthManager(context)

    fun login(username: String, password: String) {
        val request = LoginRequest(username, password)
        userRepository.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user
                    Log.d("登陆成功", "-----------------")

                    // 成功：保存本地登录状态
                    viewModelScope.launch(Dispatchers.IO) {
                        authManager.saveLoginState(true)
                        authManager.saveUser(user)
                        _loginState.value = LoginState.Success(user)
                    }
                } else {
                    _loginState.value = LoginState.Error(response.body()?.message ?: "登录失败")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _loginState.value = LoginState.Error("网络错误: ${t.localizedMessage}")
            }
        })
    }

}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}
