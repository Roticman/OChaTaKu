package com.example.ochataku.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ochataku.data.local.user.UserEntity
import com.google.gson.annotations.SerializedName

// 通用数据模型，已从 ApiService 中提取

// ---------------------- 用户管理 ----------------------
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

data class ProfileUiState(
    @SerializedName("user_id") val userId: Long = 0,
    val username: String = "",
    val avatar: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: Int = 0,
    val region: String = "",
    val signature: String = "",
    val birthDate: String = ""
)

data class UserSearchResult(
    @SerializedName("user_id") val userId: Long,
    val username: String,
    val avatar: String?
)

data class UserSimple(
    val username: String,
    val avatar: String
)

data class ChangePasswordRequest(
    val userId: Long,
    val currentPassword: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val userId: Long,
    val username: String,
    val phone: String?,
    val email: String?,
    val gender: Int?,
    val birthday: String?,
    val region: String?,
    val signature: String?
)



// ---------------------- 联系人 / 好友请求 ----------------------
data class AddContactRequest(
    val user_id: Long,
    val peer_id: Long
)

data class FriendRequest(
    @SerializedName("request_id") val id: Int,
    @SerializedName("from_user_id") val fromUserId: Long,
    @SerializedName("to_user_id") val toUserId: Long,
    @SerializedName("request_msg") val requestMsg: String,
    val status: Int
)

data class FriendRequestPayload(
    val from_user_id: Long,
    val to_user_id: Long,
    val request_msg: String = "你好，我想加你为好友"
)

data class HandleFriendRequest(
    val request_id: Int,
    val action: String // 'accept' 或 'reject'
)

data class ContactSimple(
    val user_id: Long,
    val username: String,
    val avatar: String
)

// ---------------------- 会话管理 ----------------------
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

data class ContactRequest(
    val userId: Long,
    val peerId: Long,
    val isGroup: Boolean
)

data class ContactConvResponse(
    val convId: Long
)

// ---------------------- 消息管理 ----------------------
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

data class QuotedMessage(
    val id: Long,
    val type: String,
    val preview: String,     // 文本或描述
    val mediaUrl: String? = null
)

object ChatAgentState {
    private val agentStates = mutableMapOf<Pair<Long, Long>, Pair<Boolean, String>>()
    // (userId, convId) -> (enableAI, prompt)

    fun isEnabled(userId: Long, convId: Long): Boolean =
        agentStates[userId to convId]?.first ?: false

    fun getPrompt(userId: Long, convId: Long): String =
        agentStates[userId to convId]?.second ?: ""

    fun setAgent(userId: Long, convId: Long, enabled: Boolean, prompt: String) {
        agentStates[userId to convId] = enabled to prompt
    }

    fun clear(userId: Long, convId: Long) {
        agentStates.remove(userId to convId)
    }
}


data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>
)

// ---------------------- 群组管理 ----------------------
data class GroupMember(
    val userId: Long,
    val avatar: String
)

data class GroupSimple(
    val group_name: String,
    val avatar: String
)

// ---------------------- 上传相关 ----------------------
data class UploadResponse(
    val url: String
)

// ---------------------- 展示模型 ----------------------
data class FriendRequestDisplay(
    val requestRow: FriendRequest,
    val nickname: String,
    val avatar: String
)

data class MessageDisplay(
    val id: Long,
    val conv_id: Long,
    val sender_id: Long,
    val sender_name: String,
    val sender_avatar: String,
    val content: String,
    val timestamp: Long,
    val is_group: Int,
    val message_type: String = "text",
    val media_url: String? = null,
    // ✅ 新增：本地附加的引用内容（可空）
    val quotePreview: QuotedMessage? = null
)

// ---------------------- API模型 ----------------------
data class Message(val role: String, val content: String)
data class ChatRequest(val model: String, val messages: List<Message>)
data class ChatResponse(val choices: List<Choice>)
data class Choice(val message: Message)

