package com.example.ochataku.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.conversation.*
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ConversationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationDao: ConversationDao
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDisplay>>(emptyList())
    val conversations: StateFlow<List<ConversationDisplay>> = _conversations

    fun loadConversations(userId: Long) {
        Log.d("UserId", userId.toString())

        val call = ApiClient.apiService.getConversationsAsync(userId)

        call.enqueue(object : retrofit2.Callback<List<ConversationResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<ConversationResponse>>,
                response: retrofit2.Response<List<ConversationResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val remoteList = response.body()!!.map {
                        ConversationEntity(
                            convId = it.convId,
                            userId = it.userId,
                            peerId = it.peerId,
                            isGroup = it.isGroup,
                            lastMessage = it.lastMessage ?: "",
                            timestamp = it.timestamp
                        )
                    }

                    viewModelScope.launch {
                        val enriched = remoteList.map { base ->
                            val nameAndAvatar = if (base.isGroup) {
                                fetchGroupInfo(base.peerId)  // 查询群组表
                            } else {
                                fetchUserInfo(base.peerId)   // 查询用户表
                            }

                            ConversationDisplay(
                                convId = base.convId,
                                peerId = base.peerId,
                                isGroup = base.isGroup,
                                name = nameAndAvatar.first,
                                avatar = nameAndAvatar.second,
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
                call: retrofit2.Call<List<ConversationResponse>>,
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
                Pair(user?.name ?: "用户", user?.avatar)
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
                Pair(group?.name ?: "群聊", group?.avatar)
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



