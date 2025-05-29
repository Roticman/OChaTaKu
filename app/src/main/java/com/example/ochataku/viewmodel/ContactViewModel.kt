package com.example.ochataku.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ochataku.data.local.contact.ContactEntity
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.repository.ContactRepository
import com.example.ochataku.model.ContactSimple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val contacts: StateFlow<List<ContactEntity>> = _contacts

    private val _userMap = MutableStateFlow<Map<Long, ContactSimple>>(emptyMap())
    val userMap: StateFlow<Map<Long, ContactSimple>> = _userMap

    private val _letterIndexMap = mutableStateOf<Map<String, Int>>(emptyMap())
    val letterIndexMap: State<Map<String, Int>> = _letterIndexMap


    fun loadContacts(userId: Long) {
        viewModelScope.launch {
            // 1. 获取联系人列表
            val list = contactRepository.fetchContacts(userId)

            // 2. 批量拉取用户简要信息
            val ids = list.map { it.peerId }.distinct()
            val users: List<ContactSimple> = contactRepository.getUsersByIds(ids)
            // 3. 构建 rawMap：Map<userId: Long, ContactSimple>
            val rawMap: Map<Long, ContactSimple> = users.associateBy { it.user_id }

            // 4. 填充联系人 remarkName
            val processedContacts = list.map { contact ->
                val remarkName = contact.remarkName?.takeIf { it.isNotBlank() }
                    ?: rawMap[contact.peerId]?.username.orEmpty()

                contact.copy(remarkName = remarkName)
            }

            // 5. 按照新的 remarkName 排序
            val sortedContacts = processedContacts.sortedBy {
                it.remarkName.orEmpty().firstOrNull()?.uppercase() ?: "#"
            }

            // 6. 更新 StateFlow
            _contacts.value = sortedContacts

            // 7. 同时更新 userMap（可选，如果你的界面用得到）
            val sortedUserMap = users.associateBy { it.user_id }
            _userMap.value = sortedUserMap
        }
    }

    fun updateLetterIndexMap(map: Map<String, Int>) {
        _letterIndexMap.value = map
    }

    suspend fun scrollToLetter(letter: String, listState: LazyListState) {
        val index = _letterIndexMap.value[letter]
        if (index != null) {
            listState.scrollToItem(index)
        }
    }

    fun addContact(peerId: Long) {
        val userId = authManager.getUserId() // 获取当前用户id
        viewModelScope.launch {
            contactRepository.addContact(userId, peerId) // repository 负责调API或操作数据库
            loadContacts(userId) // 添加成功后重新加载联系人
        }
    }


}
