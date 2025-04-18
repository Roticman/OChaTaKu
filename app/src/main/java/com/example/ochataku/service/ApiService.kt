package com.example.ochataku.service

import android.net.Uri
import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.service.ApiClient.apiService
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @Multipart
    @POST("/api/user/register")
    fun registerUserWithAvatar(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Call<RegisterResponse>

    @POST("/api/user/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>


    @POST("/api/conversation/add")
    suspend fun addConversation(@Body request: ConversationRequest): Response<Unit>

    @GET("/api/conversation/list/{userId}")
    fun getConversationsAsync(@Path("userId") userId: Long): Call<List<ConversationResponse>>


    // 上传媒体文件（图片、音频、视频）
    @Multipart
    @POST("/api/uploads/messages")
    fun uploadMedia(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // 发送消息（支持带 media_url 的 JSON 格式）
    @POST("/api/message/send")
    fun sendMessage(
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    // 拉取私聊消息
    @GET("/api/message/private/{user1}/{user2}")
    fun getPrivateMessages(
        @Path("user1") user1: Long,
        @Path("user2") user2: Long
    ): Call<List<MessageResponse>>

    // 拉取群聊消息
    @GET("/api/message/group/{groupId}")
    fun getGroupMessages(
        @Path("groupId") groupId: Long
    ): Call<List<MessageResponse>>
}

data class UploadResponse(
    val url: String
)

suspend fun refreshConversationAfterMessage(
    userId: Long,
    peerId: Long,
    peerName: String,
    isGroup: Boolean,
    lastMessage: String
): Boolean {
    val request = ConversationRequest(
        userId = userId,
        peerId = peerId,
        peerName = peerName,
        isGroup = isGroup,
        lastMessage = lastMessage,
        timestamp = System.currentTimeMillis()
    )
    return try {
        val response = apiService.addConversation(request)
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
data class RegisterRequest(
    val username: String,
    val password: String,
    val avatarUri: String
)

data class RegisterResponse(
    val message: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user: UserEntity
)

data class LoginUser(
    val id: Long,
    val username: String,
    val avatar: String?
)

// 请求体模型（发送会话）
data class ConversationRequest(
    val userId: Long,
    val peerId: Long,
    val peerName: String,
    val isGroup: Boolean,
    val lastMessage: String,
    val timestamp: Long
)

// 响应模型（列表）
data class ConversationResponse(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("peer_id") val peerId: Long,
    @SerializedName("peer_name") val peerName: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("is_group") val isGroupRaw: Int,
    @SerializedName("last_message") val lastMessage: String?,
    val timestamp: Long
) {
    val isGroup: Boolean get() = isGroupRaw == 1
}

// ✅ MessageResponse.kt（前端接收模型）
data class MessageResponse(
    val sender_id: Long,
    val sender_name: String?,
    val receiver_id: Long,
    val is_group: Boolean,
    val content: String,
    val timestamp: Long,
    val message_type: String = "text",
    val media_url: String? = null
)

//data class SendMessageRequest(
//    val sender_id: Long,
//    val sender_name: String?,
//    val receiver_id: Long,
//    val is_group: Boolean,
//    val content: String,
//    val timestamp: Long,
//    val message_type: String = "text",
//    val media_url: String? = null
//)

