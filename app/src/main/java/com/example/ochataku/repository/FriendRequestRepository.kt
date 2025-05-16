package com.example.ochataku.repository

import android.util.Log
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.FriendRequest
import com.example.ochataku.service.FriendRequestDisplay
import com.example.ochataku.service.HandleFriendRequest
import javax.inject.Inject

class FriendRequestRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    suspend fun getFriendRequestsWithProfile(): List<FriendRequestDisplay> {
        val userId = authManager.getUserId()

        val requests = try {
            apiService.getFriendRequests(to_user_id = userId)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()  // 请求失败时返回空列表，避免 UI 报错
        }

        if (requests.isEmpty()) return emptyList() // 没有好友请求，直接返回

        return requests.map { request ->
            try {
                val response = apiService.getUserById(request.fromUserId)
                val user = if (response.isSuccessful) response.body() else null
                FriendRequestDisplay(
                    requestRow = request,
                    nickname = user?.username ?: "用户",
                    avatar = user?.avatar ?: ""
                )
            } catch (e: Exception) {
                e.printStackTrace()
                FriendRequestDisplay(
                    requestRow = request,
                    nickname = "用户",
                    avatar = ""
                )
            }
        }
    }



    suspend fun handleFriendRequest(requestId: Int, action: String) {
        val request = HandleFriendRequest(requestId, action)
        apiService.handleFriendRequest(request)
    }
}
