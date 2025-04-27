package com.example.ochataku.repository

import com.example.ochataku.service.ApiService
import com.example.ochataku.service.UploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: ApiService
) {
    /** 上传群头像 */
    fun uploadGroupAvatar(part: MultipartBody.Part): Call<UploadResponse> =
        apiService.uploadGroupAvatar(part)

    /** 更新群头像信息 */
    fun updateGroupAvatar(request: okhttp3.RequestBody): Call<ResponseBody> =
        apiService.updateGroupAvatar(request)
}
