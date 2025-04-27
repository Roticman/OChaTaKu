package com.example.ochataku.repository


import com.example.ochataku.data.local.message.MessageDao
import com.example.ochataku.data.local.message.MessageEntity
import com.example.ochataku.service.ApiService
import com.example.ochataku.service.MessageResponse
import com.example.ochataku.service.SendMessageRequest
import com.example.ochataku.service.UploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val apiService: ApiService,
    private val messageDao: MessageDao
) {
    /** 获取会话消息 */
    fun getMessagesForConversation(convId: Long): Call<List<MessageResponse>> =
        apiService.getMessagesForConversation(convId)

    /** 发送消息 请求 */
    fun sendMessage(request: SendMessageRequest): Call<ResponseBody> =
        apiService.sendMessage(request)

    /** 上传图片 */
    fun uploadImage(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadImage(part)

    /** 上传语音 */
    fun uploadVoice(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadVoice(part)

    /** 上传视频 */
    fun uploadVideo(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadVideo(part)

    suspend fun insertMessages(messages: List<MessageEntity>) =
        messageDao.insertMessages(messages)

}
