// ✅ ChatViewModel.kt
package com.example.ochataku.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.ochataku.data.local.MessageEntity
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.MessageResponse
import com.example.ochataku.service.UploadResponse

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(

) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages
    private val apiService= ApiClient.apiService
    fun loadMessages(currentUserId: Long, peerId: Long, isGroup: Boolean) {
        val call = if (isGroup) {
            apiService.getGroupMessages(peerId)
        } else {
            apiService.getPrivateMessages(currentUserId, peerId)
        }

        call.enqueue(object : Callback<List<MessageResponse>> {
            override fun onResponse(
                call: Call<List<MessageResponse>>,
                response: Response<List<MessageResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val mapped = response.body()!!.map {
                        MessageEntity(
                            id = 0L,
                            senderId = it.sender_id,
                            senderName = it.sender_name,
                            receiverId = it.receiver_id,
                            isGroup = it.is_group,
                            content = it.content,
                            timestamp = it.timestamp,
                            messageType = it.message_type ?: "text",
                            mediaUrl = it.media_url
                        )
                    }
                    _messages.value = mapped
                }
            }

            override fun onFailure(call: Call<List<MessageResponse>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun sendMessageWithOptionalFile(
        context: Context,
        uri: Uri?,
        messageType: String,
        content: String,
        senderId: Long,
        senderName: String?,
        receiverId: Long,
        isGroup: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        if (uri != null) {
            uploadMediaFile(uri, context,
                onSuccess = { mediaUrl ->
                    sendToBackend(
                        senderId, senderName, receiverId, isGroup,
                        content, messageType, mediaUrl, onResult
                    )
                },
                onError = {
                    onResult(false)
                }
            )
        } else {
            sendToBackend(senderId, senderName, receiverId, isGroup, content, "text", null, onResult)
        }
    }

    private fun sendToBackend(
        senderId: Long,
        senderName: String?,
        receiverId: Long,
        isGroup: Boolean,
        content: String,
        messageType: String,
        mediaUrl: String?,
        onResult: (Boolean) -> Unit
    ) {
        val json = """
            {
              "sender_id": $senderId,
              "sender_name": "${senderName ?: ""}",
              "receiver_id": $receiverId,
              "is_group": $isGroup,
              "content": "$content",
              "timestamp": ${System.currentTimeMillis()},
              "message_type": "$messageType",
              "media_url": ${if (mediaUrl != null) "\"$mediaUrl\"" else null}
            }
        """
        val body = json.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())

        apiService.sendMessage(body).enqueue(object : Callback<ResponseBody> {
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
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
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