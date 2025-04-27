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
    private val conversationDao: ConversationDao,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationDisplay>>(emptyList())
    val conversations: StateFlow<List<ConversationDisplay>> = _conversations

    // convId -> 群成员头像列表
    private val _groupMembersMap = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val groupMembersMap: StateFlow<Map<Long, List<String>>> = _groupMembersMap.asStateFlow()

    // —— 新增：convId -> 已合成并上传后的群头像 URL
    private val _groupAvatarMap = MutableStateFlow<Map<Long, String?>>(emptyMap())
    val groupAvatarMap: StateFlow<Map<Long, String?>> = _groupAvatarMap.asStateFlow()


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
                Log.e("mergeAndUploadGroupAvatar", "出错：${e.message}")
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

        val call = conversationRepository.getConversations(userId)

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

//    // 加载单个群聊的成员头像
//    fun loadGroupMembers(convId: Long) {
//        viewModelScope.launch {
//            try {
//                val members: List<GroupMember> = conversationRepository.getGroupMembersAsync(convId) // suspend 函数
//                val avatars = members.map { it.avatar }
//                _groupMembersMap.update { it + (convId to avatars) }
//            } catch (e: Exception) {
//                // 出错时保持原状态或记录日志
//                e.printStackTrace()
//            }
//        }
//    }

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

    private suspend fun fetchGroupInfo(groupId: Long): Pair<String, String?> {
        return try {
            val response = conversationRepository.getGroupById(groupId)
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

    private fun loadLocalFallback(userId: Long) {
        viewModelScope.launch {
            _conversations.value = conversationRepository.getDisplayConversations(userId)
        }
    }

}



