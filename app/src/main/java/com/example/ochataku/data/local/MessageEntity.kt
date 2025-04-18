// ✅ MessageEntity.kt
package com.example.ochataku.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sender_id")
    val senderId: Long,

    @ColumnInfo(name = "sender_name")
    val senderName: String?,

    @ColumnInfo(name = "receiver_id")
    val receiverId: Long,

    @ColumnInfo(name = "is_group")
    val isGroup: Boolean,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "message_type")
    val messageType: String = "text",  // 可选值：text, image, audio, video

    @ColumnInfo(name = "media_url")
    val mediaUrl: String? = null

)
