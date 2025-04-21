package com.example.ochataku.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.RegisterRequest
import com.example.ochataku.service.RegisterResponse
import com.example.ochataku.service.UploadResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userDao: UserDao  // 由 Hilt 自动注入
) : ViewModel() {

    private val _registerSuccess = MutableStateFlow<Boolean?>(null)
    val registerSuccess: StateFlow<Boolean?> = _registerSuccess

    private val apiService = ApiClient.apiService


    fun registerUser(
        context: Context,
        username: String,
        password: String,
        avatarUri: Uri?
    ) {
        // 先上传 avatar，如果存在
        if (avatarUri != null) {
            val file = uriToFile(context, avatarUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val avatarPart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            apiService.uploadAvatar(avatarPart)
                .enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(
                        call: Call<UploadResponse>,
                        response: Response<UploadResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            // 上传成功，拿到 URL 后再注册
                            sendRegister(username, password, response.body()!!.url)
                        } else {
                            _registerSuccess.value = false
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        _registerSuccess.value = false
                    }
                })
        } else {
            // 没有头像，直接注册
            sendRegister(username, password, "")
        }
//        val usernameBody = username.toRequestBody("text/plain".toMediaType())
//        val passwordBody = password.toRequestBody("text/plain".toMediaType())
//
//        val avatarPart: MultipartBody.Part? = avatarUri?.let { uri ->
//            val file = uriToFile(context, uri)
//            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//            MultipartBody.Part.createFormData("avatar", file.name, requestFile)
//        }
//
//        // 向后端发送注册请求
//        apiService.registerUserWithAvatar(
//            username = usernameBody,
//            password = passwordBody,
//            avatar = avatarPart,
//        ).enqueue(object : Callback<RegisterResponse> {
//            override fun onResponse(
//                call: Call<RegisterResponse>,
//                response: Response<RegisterResponse>
//            ) {
//                if (response.isSuccessful) {
//                    // 后端注册成功后，保存用户到本地数据库
//                    viewModelScope.launch(Dispatchers.IO) {
////                        userDao.insertUser(user)
//                        _registerSuccess.value = true
//                        delay(2000)  // 延时2秒后重置状态
//                        resetState()
//                    }
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    Log.e("Register", "注册失败: $errorBody")
//                    _registerSuccess.value = false
//                }
//            }
//
//            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
//                _registerSuccess.value = false
//            }
//        })
    }

    private fun sendRegister(username: String, password: String, avatarUrl: String) {
        val request = RegisterRequest(
            username  = username,
            password  = password,
            avatarUri = avatarUrl
        )
        apiService.registerUser(request)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    _registerSuccess.value = response.isSuccessful
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    _registerSuccess.value = false
                }
            })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }


    fun resetState() {
        _registerSuccess.value = null
    }
}