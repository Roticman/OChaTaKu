package com.example.ochataku.service

import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.service.ApiClient.apiService
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

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

    @GET("/api/user/simple/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<DataSimple>

    @GET("/api/group/simple/{id}")
    suspend fun getGroupById(@Path("id") groupId: Long): Response<DataSimple>


    // 上传媒体文件（图片、音频、视频）
    @Multipart
    @POST("/api/uploads/messages")
    fun uploadMedia(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // ✅ 使用类型安全的发送消息请求体
    @POST("/api/message/send")
    fun sendMessage(
        @Body request: SendMessageRequest
    ): Call<ResponseBody>

    // ✅ 使用 conv_id 拉取私聊/群聊消息（统一结构）
    @GET("/api/message/conversation/{convId}")
    fun getMessagesForConversation(
        @Path("convId") convId: Long
    ): Call<List<MessageResponse>>
}

data class DataSimple(
    val name: String,
    val avatar: String
)

// ✅ 请求体：注册
data class RegisterRequest(
    val username: String,
    val password: String,
    val avatarUri: String
)

data class RegisterResponse(
    val message: String
)

// ✅ 请求体：登录
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user: UserEntity
)

// ✅ 请求体：创建或更新会话
data class ConversationRequest(
    val userId: Long,
    val peerId: Long,
    val peerName: String,
    val isGroup: Boolean,
    val lastMessage: String,
    val timestamp: Long
)

// ✅ 响应体：获取会话列表
data class ConversationResponse(
    @SerializedName("conv_id") val convId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("peer_id") val peerId: Long,
    @SerializedName("is_group") val isGroupRaw: Int,
    @SerializedName("last_message") val lastMessage: String?,
    val timestamp: Long
) {
    val isGroup: Boolean get() = isGroupRaw == 1
}

// ✅ 请求体：发送消息（已适配 conv_id）
data class SendMessageRequest(
    val sender_id: Long,
    val conv_id: Long,
    val is_group: Boolean,
    val content: String,
    val timestamp: Long,
    val message_type: String = "text",
    val media_url: String? = null
)

// ✅ 响应体：消息内容
data class MessageResponse(
    val id: Long,
    val sender_id: Long,
    val conv_id: Long,
    val is_group: Int,
    val content: String,
    val timestamp: Long,
    val message_type: String = "text",
    val media_url: String? = null
)

// ✅ 上传响应
data class UploadResponse(
    val url: String
)

// ✅ 会话刷新工具函数
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
