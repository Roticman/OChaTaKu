package com.example.ochataku.repository

import com.example.ochataku.data.local.conversation.ConversationDao
import com.example.ochataku.data.local.conversation.ConversationDisplay
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.ConversationRequest
import com.example.ochataku.service.ConversationResponse
import com.example.ochataku.service.GroupMember
import com.example.ochataku.service.GroupSimple
import com.example.ochataku.service.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val apiService: ApiService,
    private val conversationDao: ConversationDao
) {
    /** 获取会话列表（异步调用） */
    fun getConversations(userId: Long): Call<List<ConversationResponse>> =
        apiService.getConversationsAsync(userId)

    /** 获取群成员列表（挂起调用） */
    suspend fun getGroupMembers(convId: Long): List<GroupMember> =
        apiService.getGroupMembersAsync(convId)

    /** 根据群组ID获取群组简要信息 */
    suspend fun getGroupById(groupId: Long): Response<GroupSimple> =
        apiService.getGroupById(groupId)

    /** 上传群头像 */
    fun uploadGroupAvatar(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadGroupAvatar(part)

    /** 更新群头像信息 */
    fun updateGroupAvatar(request: RequestBody): Call<ResponseBody> =
        apiService.updateGroupAvatar(request)

    /** 添加会话 */
    suspend fun addConversation(request: ConversationRequest): Response<Unit> =
        apiService.addConversation(request)

    suspend fun getDisplayConversations(userId: Long): List<ConversationDisplay> =
        conversationDao.getDisplayConversations(userId)
}
