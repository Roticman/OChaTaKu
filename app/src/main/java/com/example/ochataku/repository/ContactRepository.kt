package com.example.ochataku.repository

import com.example.ochataku.data.local.contact.ContactDao
import com.example.ochataku.data.local.contact.ContactEntity
import com.example.ochataku.data.local.group.GroupDao
import com.example.ochataku.data.local.group.GroupEntity
import com.example.ochataku.service.AddContactRequest
import com.example.ochataku.service.ApiClient.apiService
import com.example.ochataku.service.ContactSimple
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao,

) {
    suspend fun fetchContacts(userId: Long): List<ContactEntity> {
        return try {
            val contacts = apiService.getContacts(userId)
            contactDao.clearContacts()         // ❶ 清空旧数据
            contactDao.insertContacts(contacts)
            contactDao.getAllContacts()
        } catch (e: Exception) {
            // 网络失败时 fallback 使用本地数据
            e.printStackTrace()
            contactDao.getAllContacts()
        }
    }

    /** 根据一组用户 ID 并发拉取用户简要信息 */
    suspend fun getUsersByIds(ids: List<Long>): List<ContactSimple> =
        ids.mapNotNull { id ->
            val resp = apiService.getContactsById(id)
            if (resp.isSuccessful) resp.body() else null
        }

    suspend fun addContact(userId: Long, peerId: Long) {
        val request = AddContactRequest(user_id = userId, peer_id = peerId)
        apiService.addContact(request) // 假设你有对应的API
    }

}
