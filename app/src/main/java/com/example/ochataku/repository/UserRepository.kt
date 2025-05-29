package com.example.ochataku.repository


import android.util.Log
import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiService
import com.example.ochataku.model.FriendRequestPayload
import com.example.ochataku.model.LoginRequest
import com.example.ochataku.model.LoginResponse
import com.example.ochataku.model.ProfileUiState
import com.example.ochataku.model.RegisterRequest
import com.example.ochataku.model.RegisterResponse
import com.example.ochataku.model.UploadResponse
import com.example.ochataku.model.UserSearchResult
import com.example.ochataku.model.UserSimple
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

    suspend fun searchUsers(query: String): List<UserSearchResult> {
        if (query.isBlank()) return emptyList()

        return try {
            val results = apiService.searchUsers(mapOf("query" to query))

            Log.d("UserRepository", "✅ 搜索成功, 返回 ${results.size} 条记录")
            results
        } catch (e: retrofit2.HttpException) {
            Log.e("UserRepository", "❌ HTTP 错误: ${e.code()} ${e.message()}")
            emptyList()
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ 搜索失败: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun sendFriendRequest(toUserId: Long) {
        val fromUserId = authManager.getUserId()
        val payload = FriendRequestPayload(
            from_user_id = fromUserId,
            to_user_id = toUserId
        )
        apiService.sendFriendRequest(payload)
    }

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
