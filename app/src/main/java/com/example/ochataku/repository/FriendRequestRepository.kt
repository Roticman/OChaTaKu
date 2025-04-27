package com.example.ochataku.repository


import com.example.ochataku.api.ApiService
import com.example.ochataku.model.FriendRequest

class FriendRequestRepository(private val apiService: ApiService) {
    suspend fun getFriendRequests(): List<FriendRequest> {
        return apiService.getFriendRequests()
    }

    suspend fun handleFriendRequest(requestId: Int, action: String) {
        apiService.handleFriendRequest(requestId, action)
    }
}
