package com.example.ochataku.repository

import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ProfileUiState
import javax.inject.Inject

class ContactProfileRepository @Inject constructor(){

    suspend fun getContactProfile(userId: Long): ProfileUiState? {
        val response = ApiClient.apiService.getProfile(userId)
        return if (response.isSuccessful) response.body() else null
    }

}
