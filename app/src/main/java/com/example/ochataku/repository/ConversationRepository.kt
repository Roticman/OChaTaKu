package com.example.ochataku.repository

import com.example.ochataku.data.local.conversation.ConversationDao
import com.example.ochataku.data.local.conversation.ConversationDisplay
import com.example.ochataku.data.local.conversation.ConversationEntity
import com.example.ochataku.service.ApiService
import com.example.ochataku.model.ContactConvResponse
import com.example.ochataku.model.ContactRequest
import com.example.ochataku.model.ConversationRequest
import com.example.ochataku.model.ConversationResponse
import com.example.ochataku.model.GroupMember
import com.example.ochataku.model.GroupSimple
import com.example.ochataku.model.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun fetchAndCacheConversations(userId: Long): List<ConversationDisplay> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getConversationsAsync(userId).execute()
                if (response.isSuccessful && response.body() != null) {
                    val conversations = response.body()!!

                    conversationDao.clearConversationsForUser(userId)

                    val localEntities = conversations.map {
                        ConversationEntity(
                            convId = it.convId,
                            aId = it.aId,
                            bId = it.bId,
                            groupId = it.groupId,
                            isGroup = it.isGroup,
                            lastMessage = it.lastMessage ?: "",
                            timestamp = it.timestamp
                        )
                    }

                    conversationDao.insertAll(localEntities)
                }

                // 从 Room 中读取基础数据，再逐条补全 name 和 avatar
                val baseList = conversationDao.getConversationsForUser(userId)

                baseList.map { convo ->
                    val targetId = if (convo.isGroup) convo.groupId!! else {
                        if (convo.aId == userId) convo.bId!! else convo.aId!!
                    }

                    val (name, avatar) = getNameAndAvatar(targetId, convo.isGroup)

                    ConversationDisplay(
                        convId = convo.convId,
                        aId = convo.aId,
                        bId = convo.bId,
                        groupId = convo.groupId,
                        isGroup = convo.isGroup,
                        name = name,
                        avatar = avatar ?: "",
                        lastMessage = convo.lastMessage,
                        timestamp = convo.timestamp
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()


            // fallback 到 Room 原有数据并补充名称与头像
            val baseList = conversationDao.getConversationsForUser(userId)

            baseList.map { convo ->
                val targetId = if (convo.isGroup) convo.groupId!! else {
                    if (convo.aId == userId) convo.bId!! else convo.aId!!
                }

                val (name, avatar) = getNameAndAvatar(targetId, convo.isGroup)

                ConversationDisplay(
                    convId = convo.convId,
                    aId = convo.aId,
                    bId = convo.bId,
                    groupId = convo.groupId,
                    isGroup = convo.isGroup,
                    name = name,
                    avatar = avatar ?: "",
                    lastMessage = convo.lastMessage,
                    timestamp = convo.timestamp
                )
            }
        }
    }

    suspend fun getOrCreateConversation(request: ContactRequest): ContactConvResponse {
        return apiService.getOrCreateConversation(request)
    }


    /** 根据 isGroup 判断是私聊还是群聊，获取名称与头像 */
    private suspend fun getNameAndAvatar(id: Long, isGroup: Boolean): Pair<String, String?> {
        return if (isGroup) {
            getGroupNameAndAvatar(id)
        } else {
            getUserNameAndAvatar(id)
        }
    }

    private suspend fun getUserNameAndAvatar(userId: Long): Pair<String, String?> {
        return try {
            val response = apiService.getUserById(userId)
            if (response.isSuccessful) {
                val user = response.body()
                Pair(user?.username ?: "用户", user?.avatar)
            } else {
                Pair("未知用户", null)
            }
        } catch (e: Exception) {
            Pair("未知用户", null)
        }
    }

    private suspend fun getGroupNameAndAvatar(groupId: Long): Pair<String, String?> {
        return try {
            val response = apiService.getGroupById(groupId)
            if (response.isSuccessful) {
                val group = response.body()
                Pair(group?.group_name ?: "群聊", group?.avatar)
            } else {
                Pair("群聊", null)
            }
        } catch (e: Exception) {
            Pair("群聊", null)
        }
    }

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
