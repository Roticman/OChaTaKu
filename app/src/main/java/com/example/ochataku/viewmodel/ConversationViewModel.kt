package com.example.ochataku.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ochataku.data.local.conversation.ConversationDao
import com.example.ochataku.data.local.conversation.ConversationDisplay
import com.example.ochataku.data.local.conversation.ConversationEntity
import com.example.ochataku.repository.ConversationRepository
import com.example.ochataku.repository.UserRepository
import com.example.ochataku.service.ApiClient.apiService
import com.example.ochataku.service.ConversationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDisplay>>(emptyList())
    val conversations: StateFlow<List<ConversationDisplay>> = _conversations

    // convId -> 群成员头像列表
    private val _groupMembersMap = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val groupMembersMap: StateFlow<Map<Long, List<String>>> = _groupMembersMap.asStateFlow()

    // —— 新增：convId -> 已合成并上传后的群头像 URL
    private val _groupAvatarMap = MutableStateFlow<Map<Long, String?>>(emptyMap())
    val groupAvatarMap: StateFlow<Map<Long, String?>> = _groupAvatarMap.asStateFlow()

    fun deleteConversation(convId: Long) {
        viewModelScope.launch {
            try {
                apiService.deleteConversation(mapOf("convId" to convId))
                _conversations.update { it.filterNot { conv -> conv.convId == convId } }
            } catch (e: Exception) {
                Log.e("Conversation", "删除会话失败", e)
            }
        }
    }

    fun mergeAndUploadGroupAvatar(
        groupId: Long,
        avatarUrls: List<String>
    ) {
        viewModelScope.launch {
            try {
                val urls = avatarUrls.take(9) // 最多取9个头像

                val bitmaps = withContext(Dispatchers.IO) {
                    urls.mapNotNull { url ->
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(url)
                                .allowHardware(false)
                                .build()
                            val result = context.imageLoader.execute(request)
                            if (result is SuccessResult) {
                                result.drawable.toBitmap()
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                if (bitmaps.isEmpty()) {
                    throw Exception("没有成功加载任何头像")
                }

                val mergedBitmap = createMergedBitmap(bitmaps)

                // 保存到临时文件
                val file = File(context.cacheDir, "group_${groupId}_avatar.jpg")
                val outputStream = FileOutputStream(file)
                mergedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // 上传合成头像
                val uploadResponse = conversationRepository.uploadGroupAvatar(body).execute()
                if (!uploadResponse.isSuccessful) {
                    throw Exception("上传群头像失败")
                }
                val avatarUrl = uploadResponse.body()?.url ?: throw Exception("未返回头像URL")

                // 更新群资料
                val json = JSONObject().apply {
                    put("group_id", groupId)
                    put("avatar_url", avatarUrl)
                }
                val updateResponse = conversationRepository.updateGroupAvatar(
                    json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                ).execute()

                if (!updateResponse.isSuccessful) {
                    throw Exception("更新群头像失败")
                }

                // ✅ 更新本地State，刷新UI
                _groupAvatarMap.update { it + (groupId to avatarUrl) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun createMergedBitmap(bitmaps: List<Bitmap>): Bitmap {
        val size = 300 // 最终生成的图片大小
        val count = bitmaps.size
        val cols = when {
            count <= 1 -> 1
            count <= 4 -> 2
            else -> 3
        }
        val rows = (count + cols - 1) / cols
        val itemSize = size / cols

        val mergedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mergedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (i in bitmaps.indices) {
            val bitmap = Bitmap.createScaledBitmap(bitmaps[i], itemSize, itemSize, true)
            val col = i % cols
            val row = i / cols
            val left = col * itemSize
            val top = row * itemSize
            canvas.drawBitmap(bitmap, null, Rect(left, top, left + itemSize, top + itemSize), paint)
        }
        return mergedBitmap
    }


    fun loadConversations(userId: Long) {
        viewModelScope.launch {
            try {
                val list = conversationRepository.fetchAndCacheConversations(userId)
                _conversations.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _conversations.value = conversationRepository.getDisplayConversations(userId)
            }
        }
    }
}



