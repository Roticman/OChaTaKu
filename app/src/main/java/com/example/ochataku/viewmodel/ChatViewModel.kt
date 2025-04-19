package com.example.ochataku.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.message.MessageDisplay
import com.example.ochataku.data.local.message.MessageEntity
import com.example.ochataku.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageDisplay>>(emptyList())
    val messages: StateFlow<List<MessageDisplay>> = _messages
    private val apiService = ApiClient.apiService

    fun loadMessagesByConvId(
        convId: Long,
        onError: (errorMsg: String) -> Unit = {}
    ) {
        apiService.getMessagesForConversation(convId).enqueue(object : Callback<List<MessageResponse>> {
            override fun onResponse(
                call: Call<List<MessageResponse>>,
                response: Response<List<MessageResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val mapped = response.body()!!.map {
                        MessageEntity(
                            id = it.id,
                            senderId = it.sender_id,
                            convId = it.conv_id,
                            isGroup = it.is_group != 0,
                            content = it.content,
                            timestamp = it.timestamp,
                            messageType = it.message_type,
                            mediaUrl = it.media_url
                        )
                    }
                    viewModelScope.launch {
                        val enriched = mapped.map { base ->
                            val nameAndAvatar = fetchUserInfo(base.senderId)
                            MessageDisplay(
                                id = base.id,
                                senderId = base.senderId,
                                name = nameAndAvatar.first,
                                avatar = nameAndAvatar.second,
                                convId = base.convId,
                                isGroup = base.isGroup,
                                content = base.content,
                                timestamp = base.timestamp,
                                messageType = base.messageType,
                                mediaUrl = base.mediaUrl
                            )
                        }
                        _messages.value = enriched
                    }
                } else {
                    val code = response.code()
                    val msg = response.message()
                    val body = response.errorBody()?.string()
                    Log.e("ChatViewModel", "加载消息失败 code=$code, body=$body")
                    onError("加载消息失败：code=$code")
                }
            }

            override fun onFailure(call: Call<List<MessageResponse>>, t: Throwable) {
                Log.e("ChatViewModel", "网络异常", t)
                onError("网络异常：${t.localizedMessage ?: "未知错误"}")
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

    fun sendMessageWithOptionalFile(
        context: Context,
        uri: Uri?,
        messageType: String,
        content: String,
        senderId: Long,
        convId: Long,
        isGroup: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        if (uri != null) {
            uploadMediaFile(uri, context,
                onSuccess = { mediaUrl ->
                    sendToBackend(
                        senderId, convId, isGroup, content, messageType, mediaUrl, onResult
                    )
                },
                onError = {
                    onResult(false)
                }
            )
        } else {
            sendToBackend(
                senderId, convId, isGroup, content, "text", null, onResult
            )
        }
    }

    private fun sendToBackend(
        senderId: Long,
        convId: Long,
        isGroup: Boolean,
        content: String,
        messageType: String,
        mediaUrl: String?,
        onResult: (Boolean) -> Unit
    ) {
        val request = SendMessageRequest(
            sender_id = senderId,
            conv_id = convId,
            is_group = isGroup,
            content = content,
            timestamp = System.currentTimeMillis(),
            message_type = messageType,
            media_url = mediaUrl
        )

        apiService.sendMessage(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onResult(response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                onResult(false)
            }
        })
    }

    private fun uploadMediaFile(
        uri: Uri,
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val file = uriToFile(context, uri)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        apiService.uploadMedia(body).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!.url)
                } else {
                    onError("上传失败")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                onError("网络错误: ${t.message}")
            }
        })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("upload", ".tmp", context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
