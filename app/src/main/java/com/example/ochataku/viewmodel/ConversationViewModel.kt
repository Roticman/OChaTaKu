package com.example.ochataku.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.*
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

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    fun loadConversations(userId: Long) {
        Log.d("UserId", userId.toString())

        val call = ApiClient.apiService.getConversationsAsync(userId) // 👈 用 Call 类型接口

        call.enqueue(object : retrofit2.Callback<List<ConversationResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<ConversationResponse>>,
                response: retrofit2.Response<List<ConversationResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val remoteList = response.body()!!.map {
                        Conversation(
                            id = it.id,
                            userId = it.userId,
                            peerId = it.peerId,
                            peerName = it.peerName,
                            avatar = it.avatar,
                            isGroup = it.isGroup,
                            lastMessage = it.lastMessage ?: "",
                            timestamp = it.timestamp
                        )
                    }

                    // 打印调试
                    remoteList.forEach {
                        Log.d("Conversation", it.toString())
                    }

                    // 保存并更新 UI
                    viewModelScope.launch {
                        conversationDao.clearConversationsForUser(userId)
                        conversationDao.insertAll(remoteList)
                        _conversations.value = remoteList
                    }
                } else {
                    Log.e("loadConversations", "请求失败: ${response.code()} - ${response.errorBody()?.string()}")
                    viewModelScope.launch {
                        _conversations.value = conversationDao.getConversationsForUser(userId)
                    }
                }
            }

            override fun onFailure(
                call: retrofit2.Call<List<ConversationResponse>>,
                t: Throwable
            ) {
                Log.e("loadConversations", "网络错误：${t.message}")
                viewModelScope.launch {
                    _conversations.value = conversationDao.getConversationsForUser(userId)
                }
            }
        })
    }



}
