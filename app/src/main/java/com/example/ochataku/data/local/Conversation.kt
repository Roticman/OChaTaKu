package com.example.ochataku.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,         // 当前用户 ID
    val peerId: Long,         // 对话对象 ID（可能是用户，也可能是群组）
    val peerName: String?,     // 对方用户名或群组名
    val avatar: String?,
    val isGroup: Boolean,     // 是否为群组会话
    val lastMessage: String,  // 最后一条消息
    val timestamp: Long       // 最后更新时间（毫秒）
)
