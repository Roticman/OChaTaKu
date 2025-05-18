package com.example.ochataku.service


import com.example.ochataku.data.local.contact.ContactEntity
import com.example.ochataku.data.local.group.GroupEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ---------------------- 用户管理 ----------------------
    @POST("/api/user/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/user/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/api/user/profile/{id}")
    suspend fun getProfile(@Path("id") userId: Long): Response<ProfileUiState>

    @POST("/api/user/search_user")
    suspend fun searchUsers(@Body body: Map<String, String>): List<UserSearchResult>

    @POST("/api/user/deactivate/{id}")
    suspend fun deactivateAccount(@Path("id") userId: Long): Response<Unit>

    @POST("/api/user/change_password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>



    // ---------------------- 联系人管理 ----------------------
    @GET("/api/contact/{userId}")
    suspend fun getContacts(@Path("userId") userId: Long): List<ContactEntity>

    @GET("/api/contact/contact_simple/{id}")
    suspend fun getContactsById(@Path("id") userId: Long): Response<ContactSimple>

    @POST("/api/contact/add_contact")
    suspend fun addContact(@Body request: AddContactRequest)

    @POST("/api/contact/send_request")
    suspend fun sendFriendRequest(@Body request: FriendRequestPayload): Response<Unit>

    @GET("/api/contact/friend_requests/{id}")
    suspend fun getFriendRequests(@Path("id") to_user_id: Long): List<FriendRequest>

    @POST("/api/contact/handle_request")
    suspend fun handleFriendRequest(@Body request: HandleFriendRequest)



    // ---------------------- 会话管理 ----------------------
    @POST("/api/conversation/get_or_create")
    suspend fun getOrCreateConversation(@Body request: ContactRequest): ContactConvResponse

    @POST("/api/conversation/add")
    suspend fun addConversation(@Body request: ConversationRequest): Response<Unit>

    @GET("/api/conversation/list/{userId}")
    fun getConversationsAsync(@Path("userId") userId: Long): Call<List<ConversationResponse>>

    @GET("/api/conversation/list/group_member/{convId}")
    suspend fun getGroupMembersAsync(@Path("convId") convId: Long): List<GroupMember>

    @GET("/api/conversation/user_simple/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<UserSimple>

    @GET("/api/conversation/group_simple/{id}")
    suspend fun getGroupById(@Path("id") groupId: Long): Response<GroupSimple>

    @POST("/api/conversation/delete")
    suspend fun deleteConversation(@Body body: Map<String, Long>): Response<Unit>



    // ---------------------- 消息管理 ----------------------
    @POST("/api/message/send")
    fun sendMessage(@Body request: SendMessageRequest): Call<ResponseBody>

    @GET("/api/message/{convId}")
    fun getMessagesForConversation(@Path("convId") convId: Long): Call<List<MessageResponse>>

    @HTTP(method = "DELETE", path = "/api/message/delete/{id}", hasBody = true)
    suspend fun deleteMessage(
        @Path("id") id: Long,
        @Body userId: Map<String, Long> // mapOf("user_id" to currentUserId)
    ): Response<Unit>




    // ---------------------- 群组管理 ----------------------
    @GET("/api/group/list/{userId}")
    suspend fun getGroupsByUserId(@Path("userId") userId: Long): List<GroupEntity>

    @POST("/api/group/update_avatar")
    fun updateGroupAvatar(@Body request: RequestBody): Call<ResponseBody>



    // ---------------------- 上传相关 ----------------------
    @Multipart
    @POST("/api/upload/users/avatar")
    fun uploadAvatar(@Part file: MultipartBody.Part): Call<UploadResponse>

    @Multipart
    @POST("/api/upload/groups/avatar")
    fun uploadGroupAvatar(@Part file: MultipartBody.Part): Call<UploadResponse>

    @Multipart
    @POST("/api/upload/messages/voices")
    fun uploadVoice(@Part file: MultipartBody.Part): Call<UploadResponse>

    @Multipart
    @POST("/api/upload/messages/videos")
    fun uploadVideo(@Part file: MultipartBody.Part): Call<UploadResponse>

    @Multipart
    @POST("/api/upload/messages/images")
    fun uploadImage(@Part file: MultipartBody.Part): Call<UploadResponse>
}


