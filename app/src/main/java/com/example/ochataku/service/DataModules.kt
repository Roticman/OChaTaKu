package com.example.ochataku.service

import com.example.ochataku.data.local.user.UserEntity
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

// 通用数据模型，已从 ApiService 中提取

data class UserSimple(
    val username: String,
    val avatar: String
)

data class ContactSimple(
    val user_id: Long,
    val username: String,
    val avatar: String
)

// 假设后端返回的群成员数据模型
data class GroupMember(
    val userId: Long,
    val avatar: String
)

data class GroupSimple(
    val group_name: String,
    val avatar: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val avatarUri: String
)

// 注册响应
data class RegisterResponse(
    val message: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user: UserEntity // 请根据实际路径导入 UserEntity
)

data class ConversationRequest(
    val userId: Long,
    val peerId: Long,
    val peerName: String,
    val isGroup: Boolean,
    val lastMessage: String,
    val timestamp: Long
)

data class ConversationResponse(
    @SerializedName("conv_id") val convId: Long,
    @SerializedName("a_id") val aId: Long?,
    @SerializedName("b_id") val bId: Long?,
    @SerializedName("group_id") val groupId: Long?,
    @SerializedName("is_group") val isGroupRaw: Int,
    @SerializedName("last_message") val lastMessage: String?,
    val timestamp: Long
) {
    val isGroup: Boolean get() = isGroupRaw == 1
}

data class SendMessageRequest(
    val sender_id: Long,
    val receiver_id: Long,
    val conv_id: Long,
    val is_group: Boolean,
    val content: String,
    val timestamp: Long,
    val message_type: String = "text",
    val media_url: String? = null
)

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


data class MessageDisplay(
    val conv_id: Long,
    val sender_id: Long,
    val sender_name: String,
    val sender_avatar: String,
    val content: String,
    val timestamp: Long,
    val is_group: Int,
    val message_type: String = "text",
    val media_url: String? = null
)

data class UploadResponse(
    val url: String
)

data class ProfileUiState(
    @SerializedName("user_id") val userId: Long = 0,
    val username: String = "",
    val avatar: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: Int = 0, // 0:保密, 1:男, 2:女
    val region: String = "",
    val signature: String = "",
    val birthDate: String = ""
)

data class AddContactRequest(
    val user_id: Long,
    val peer_id: Long
)

data class FriendRequest(
    val id: Int,
    val fromUserId: String,
    val toUserId: String,
    val requestMsg: String,
    val status: Int // 0=待处理, 1=同意, 2=拒绝
)

data class HandleFriendRequest(
    val request_id: Int,
    val action: String // 'accept' 或 'reject'
)
