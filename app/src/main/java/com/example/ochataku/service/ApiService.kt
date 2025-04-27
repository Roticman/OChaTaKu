package com.example.ochataku.service


import com.example.ochataku.data.local.contact.ContactEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("/api/user/profile/{id}")
    suspend fun getProfile(@Path("id") userId: Long): Response<ProfileUiState>

    @GET("/api/contact/{userId}")
    suspend fun getContacts(@Path("userId") userId: Long): List<ContactEntity>

    // 联系人信息
    @GET("/api/contact/contact_simple/{id}")
    suspend fun getContactsById(
        @Path("id") userId: Long
    ): Response<ContactSimple>

    @POST("/add_contact")
    suspend fun addContact(@Body request: AddContactRequest)

    @GET("/friend_requests")
    suspend fun getFriendRequests(): List<FriendRequest>

    @POST("/handle_friend_request")
    suspend fun handleFriendRequest(@Body request: HandleFriendRequest)

    // 用户头像上传
    @Multipart
    @POST("/api/upload/users/avatar")
    fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // 群头像上传
    @Multipart
    @POST("/api/upload/groups/avatar")
    fun uploadGroupAvatar(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    @POST("/api/group/update_avatar")
    fun updateGroupAvatar(
        @Body request: RequestBody
    ): Call<ResponseBody>


    // 聊天语音文件上传
    @Multipart
    @POST("/api/upload/messages/voices")
    fun uploadVoice(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // 聊天视频文件上传
    @Multipart
    @POST("/api/upload/messages/videos")
    fun uploadVideo(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // 聊天图片文件上传
    @Multipart
    @POST("/api/upload/messages/images")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    // 注册与登录
    @POST("/api/user/register")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("/api/user/login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    // 会话管理
    @POST("/api/conversation/add")
    suspend fun addConversation(
        @Body request: ConversationRequest
    ): Response<Unit>

    @GET("/api/conversation/list/{userId}")
    fun getConversationsAsync(
        @Path("userId") userId: Long
    ): Call<List<ConversationResponse>>

    @GET("/api/conversation/list/group_member/{convId}")
    suspend fun getGroupMembersAsync(
        @Path("convId") convId: Long
    ): List<GroupMember>

    // 用户/群组信息
    @GET("/api/conversation/user_simple/{id}")
    suspend fun getUserById(
        @Path("id") userId: Long
    ): Response<UserSimple>

    @GET("/api/conversation/group_simple/{id}")
    suspend fun getGroupById(
        @Path("id") groupId: Long
    ): Response<GroupSimple>

    // 消息发送与拉取
    @POST("/api/message/send")
    fun sendMessage(
        @Body request: SendMessageRequest
    ): Call<ResponseBody>

    @GET("/api/message/{convId}")
    fun getMessagesForConversation(
        @Path("convId") convId: Long
    ): Call<List<MessageResponse>>


}

