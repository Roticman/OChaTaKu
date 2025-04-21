package com.example.ochataku.data.local.conversation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val convId: Long = 0,
    val aId: Long?,
    val bId: Long?,
    val groupId: Long?,
    val isGroup: Boolean,     // 是否为群组会话
    val lastMessage: String,  // 最后一条消息
    val timestamp: Long       // 最后更新时间（毫秒）
)

data class ConversationDisplay(
    val convId: Long,
    val aId: Long?,
    val bId: Long?,
    val groupId: Long?,
    val isGroup: Boolean,
    val name: String,
    val avatar: String,
    val lastMessage: String,
    val timestamp: Long
)