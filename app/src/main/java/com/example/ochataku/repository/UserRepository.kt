package com.example.ochataku.repository


import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.LoginRequest
import com.example.ochataku.service.LoginResponse
import com.example.ochataku.service.ProfileUiState
import com.example.ochataku.service.RegisterRequest
import com.example.ochataku.service.RegisterResponse
import com.example.ochataku.service.UploadResponse
import com.example.ochataku.service.UserSimple
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager,
    private val userDao: UserDao
) {
    /** 登录 */
    fun loginUser(request: LoginRequest): Call<LoginResponse> =
        apiService.loginUser(request)

    /** 注册 */
    fun registerUser(request: RegisterRequest): Call<RegisterResponse> =
        apiService.registerUser(request)

    /** 上传用户头像 */
    fun uploadAvatar(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadAvatar(part)

    /** 根据用户ID获取用户简要信息 */
    suspend fun getUserById(userId: Long): Response<UserSimple> =
        apiService.getUserById(userId)

    suspend fun getUserProfile(): ProfileUiState {
        val response = apiService.getProfile(
            userId = authManager.getUserId()
        )
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            ProfileUiState()
        }
    }
}
