package com.example.ochataku.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.message.MessageDao
import com.example.ochataku.data.local.message.MessageEntity
import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.repository.MessageRepository
import com.example.ochataku.repository.UserRepository
import com.example.ochataku.service.MessageDisplay
import com.example.ochataku.service.MessageResponse
import com.example.ochataku.service.SendMessageRequest
import com.example.ochataku.service.UploadResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.sql.Timestamp
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messageDao: MessageDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<MessageDisplay>>(emptyList())
    val messages: StateFlow<List<MessageDisplay>> = _messages

    fun addMessage(raw: MessageDisplay) {
        viewModelScope.launch {
            val (name, avatar) = fetchUserInfo(raw.sender_id)
            val completed = raw.copy(
                sender_name = name,
                sender_avatar = avatar!!
            )
            _messages.update { current -> current + completed }

            // 可选：插入本地数据库（注意转换为 MessageEntity）
            messageDao.insertMessages(
                listOf(
                    MessageEntity(
                        id = 0, // 如果你是自增主键，可传0
                        senderId = completed.sender_id,
                        convId = completed.conv_id,
                        isGroup = completed.is_group == 1,
                        content = completed.content,
                        timestamp = completed.timestamp,
                        messageType = completed.message_type,
                        mediaUrl = completed.media_url
                    )
                )
            )
        }
    }


    fun loadMessagesByConvId(
        context: Context,
        convId: Long,
        onError: (String) -> Unit = {}
    ) {
        messageRepository.getMessagesForConversation(convId)
            .enqueue(object : Callback<List<MessageResponse>> {
                override fun onResponse(
                    call: Call<List<MessageResponse>>,
                    response: Response<List<MessageResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val entities = response.body()!!.map {
                            MessageEntity(
                                id = it.id,
                                senderId = it.sender_id,
                                convId = it.conv_id,
                                isGroup = it.is_group == 1,
                                content = it.content,
                                timestamp = it.timestamp,
                                messageType = it.message_type,
                                mediaUrl = it.media_url
                            )
                        }
                        viewModelScope.launch {

                            messageDao.insertMessages(entities)
                            entities.forEach { e ->
                                e.mediaFileSize = e.mediaUrl
                                    ?.let { File(it) }
                                    ?.takeIf { it.exists() }
                                    ?.length()
                            }
                            _messages.value = entities.map { e ->
                                val nameAndAvatar = fetchUserInfo(e.senderId);
                                MessageDisplay(
                                    conv_id = e.convId,
                                    sender_id = e.senderId,
                                    sender_name = nameAndAvatar.first,
                                    sender_avatar = nameAndAvatar.second!!,
                                    content = e.content,
                                    timestamp = e.timestamp,
                                    is_group = if (e.isGroup) 1 else 0,
                                    message_type = e.messageType,
                                    media_url = e.mediaUrl
                                )

                            }

                        }
                    } else {
                        viewModelScope.launch {
                            val cached = messageDao.getMessagesForConversation(convId)
                            cached.forEach { e ->
                                e.mediaFileSize = e.mediaUrl
                                    ?.let { File(it) }
                                    ?.takeIf { it.exists() }
                                    ?.length()
                            }
                            _messages.value = cached.map { e ->
                                val nameAndAvatar = fetchUserInfo(e.senderId);
                                MessageDisplay(
                                    conv_id = e.convId,
                                    sender_id = e.senderId,
                                    sender_name = nameAndAvatar.first,
                                    sender_avatar = nameAndAvatar.second!!,
                                    content = e.content,
                                    timestamp = e.timestamp,
                                    is_group = if (e.isGroup) 1 else 0,
                                    message_type = e.messageType,
                                    media_url = e.mediaUrl
                                )
                            }
                        }
                        onError("加载失败：code=${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<MessageResponse>>, t: Throwable) {
                    viewModelScope.launch {
                        val cached = messageDao.getMessagesForConversation(convId)
                        cached.forEach { e ->
                            e.mediaFileSize = e.mediaUrl
                                ?.let { File(it) }
                                ?.takeIf { it.exists() }
                                ?.length()
                        }
                        _messages.value = cached.map { e ->
                            val sender = userDao.getUserById(e.senderId)
                            val name = sender?.username ?: "未知用户"
                            val avatar = sender?.avatar ?: ""

                            MessageDisplay(
                                conv_id = e.convId,
                                sender_id = e.senderId,
                                sender_name = name,
                                sender_avatar = avatar,
                                content = e.content,
                                timestamp = e.timestamp,
                                is_group = if (e.isGroup) 1 else 0,
                                message_type = e.messageType,
                                media_url = e.mediaUrl
                            )
                        }
                    }
                    onError("网络异常：${t.localizedMessage}")
                }
            })
    }

    fun sendMessage(
        context: Context,
        mediaUri: Uri?,
        messageType: String,
        content: String,
        senderId: Long,
        receiverId: Long,
        convId: Long,
        isGroup: Boolean,
        onResult: (Boolean) -> Unit = {}
    ) {
        if (mediaUri != null) {
            val file = uriToFile(context, mediaUri)
            val part = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            val uploadCall = when (messageType) {
                "voice" -> messageRepository.uploadVoice(part)
                "video" -> messageRepository.uploadVideo(part)
                else -> messageRepository.uploadImage(part)
            }
            uploadCall.enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        doSendMessage(
                            context = context,
                            content = content,
                            mediaUrl = response.body()!!.url,
                            messageType = messageType,
                            senderId = senderId,
                            receiverId = receiverId,
                            convId = convId,
                            isGroup = isGroup,
                            onResult = onResult
                        )
                    } else {
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    onResult(false)
                }
            })
        } else {
            doSendMessage(
                context,
                content,
                null,
                "text",
                senderId,
                receiverId,
                convId,
                isGroup,
                onResult
            )
        }
    }

    private fun doSendMessage(
        context: Context,
        content: String,
        mediaUrl: String?,
        messageType: String,
        senderId: Long,
        receiverId: Long,
        convId: Long,
        isGroup: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        val req = SendMessageRequest(
            sender_id = senderId,
            receiver_id = receiverId,
            conv_id = convId,
            is_group = isGroup,
            content = content,
            timestamp = System.currentTimeMillis(),
            message_type = messageType,
            media_url = mediaUrl
        )
        messageRepository.sendMessage(req).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onResult(response.isSuccessful)
                if (response.isSuccessful) {
                    loadMessagesByConvId(context = context, convId = convId)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false)
            }
        })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)!!
        val file = File.createTempFile("upload", null, context.cacheDir)
        input.use { it.copyTo(file.outputStream()) }
        return file
    }

    private suspend fun fetchUserInfo(userId: Long): Pair<String, String?> {
        return try {
            val response = userRepository.getUserById(userId)
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

    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(context: Context, filePath: String) {
        try {
            stopAudio() // 若已有音频在播放，先停止

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                prepare()
                start()
            }

            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                Toast.makeText(context, "播放完成", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "播放失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()
            }
        }
        mediaPlayer = null
    }

    fun getAudioDurationSeconds(context: Context, audioUrl: String): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioUrl, HashMap()) // 网络音频
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            (durationStr?.toIntOrNull()?.div(1000)) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }


}
