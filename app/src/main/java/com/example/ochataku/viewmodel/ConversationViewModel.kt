package com.example.ochataku.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.conversation.ConversationDao
import com.example.ochataku.data.local.conversation.ConversationDisplay
import com.example.ochataku.data.local.conversation.ConversationEntity
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ConversationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationDao: ConversationDao
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDisplay>>(emptyList())
    val conversations: StateFlow<List<ConversationDisplay>> = _conversations


    fun loadConversations(userId: Long) {

        val call = ApiClient.apiService.getConversationsAsync(userId)

        call.enqueue(object : Callback<List<ConversationResponse>> {
            override fun onResponse(
                call: Call<List<ConversationResponse>>,
                response: Response<List<ConversationResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val remoteList = response.body()!!.map {
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

                    viewModelScope.launch {
                        val enriched = remoteList.map { base ->

                            val nameAndAvatar = if (base.isGroup) {
                                fetchGroupInfo(base.groupId!!)  // 查询群组表
                            } else {
                                val peerId = if (base.aId == userId) base.bId else base.aId
                                fetchUserInfo(peerId!!)   // 查询用户表
                            }

                            ConversationDisplay(
                                convId = base.convId,
                                aId = base.aId,
                                bId = base.bId,
                                groupId = base.groupId,
                                isGroup = base.isGroup,
                                name = nameAndAvatar.first,
                                avatar = nameAndAvatar.second ?: "null",
                                lastMessage = base.lastMessage,
                                timestamp = base.timestamp
                            )

                        }
                        _conversations.value = enriched
                    }

                } else {
                    Log.e(
                        "loadConversations",
                        "请求失败: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                    loadLocalFallback(userId)
                }
            }

            override fun onFailure(
                call: Call<List<ConversationResponse>>,
                t: Throwable
            ) {
                Log.e("loadConversations", "网络错误：${t.message}")
                loadLocalFallback(userId)
            }
        })
    }

    private suspend fun fetchUserInfo(userId: Long): Pair<String, String?> {
        return try {
            val response = ApiClient.apiService.getUserById(userId)
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

    private suspend fun fetchGroupInfo(groupId: Long): Pair<String, String?> {
        return try {
            val response = ApiClient.apiService.getGroupById(groupId)
            if (response.isSuccessful) {
                val group = response.body()
                Pair(group?.groupName ?: "群聊", group?.avatar)
            } else {
                Pair("群聊", null)
            }
        } catch (e: Exception) {
            Pair("群聊", null)
        }
    }

    private fun loadLocalFallback(userId: Long) {
        viewModelScope.launch {
            _conversations.value = conversationDao.getDisplayConversations(userId)
        }
    }
}



