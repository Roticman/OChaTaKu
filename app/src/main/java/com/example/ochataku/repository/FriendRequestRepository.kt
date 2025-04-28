package com.example.ochataku.repository

import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.FriendRequest
import com.example.ochataku.service.HandleFriendRequest
import javax.inject.Inject

class FriendRequestRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    suspend fun getFriendRequests(): List<FriendRequest> {
        return apiService.getFriendRequests(
            to_user_id = authManager.getUserId()
        )
    }

    suspend fun handleFriendRequest(requestId: Int, action: String) {
        val request = HandleFriendRequest(requestId, action)
        apiService.handleFriendRequest(request)
    }
}
