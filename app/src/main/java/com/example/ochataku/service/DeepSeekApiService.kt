package com.example.ochataku.service

import com.example.ochataku.model.ChatRequest
import com.example.ochataku.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    fun createCompletion(@Body request: ChatRequest): Call<ChatResponse>
}

