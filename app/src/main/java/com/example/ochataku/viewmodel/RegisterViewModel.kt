package com.example.ochataku.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.ochataku.repository.UserRepository
import com.example.ochataku.model.RegisterRequest
import com.example.ochataku.model.RegisterResponse
import com.example.ochataku.model.UploadResponse
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
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _registerSuccess = MutableStateFlow<Boolean?>(null)
    val registerSuccess: StateFlow<Boolean?> = _registerSuccess


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
            val avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)

            userRepository.uploadAvatar(avatarPart)
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
    }

    private fun sendRegister(username: String, password: String, avatarUrl: String) {
        val request = RegisterRequest(
            username = username,
            password = password,
            avatarUri = avatarUrl
        )
        userRepository.registerUser(request)
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