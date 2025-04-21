// ✅ MessageEntity.kt
package com.example.ochataku.data.local.message

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sender_id") val senderId: Long,
    @ColumnInfo(name = "conv_id") val convId: Long,
    @ColumnInfo(name = "is_group") val isGroup: Boolean,
    val content: String,
    val timestamp: Long,
    @ColumnInfo(name = "message_type") val messageType: String,
    @ColumnInfo(name = "media_url") val mediaUrl: String?,
) {
    @Ignore
    var mediaFileSize: Long? = null
}

