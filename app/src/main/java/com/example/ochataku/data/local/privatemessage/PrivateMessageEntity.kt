package com.example.ochataku.data.local.privatemessage

import androidx.room.*;
import com.example.ochataku.data.local.user.UserEntity

@Entity(
    tableName = "private_messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiver_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receiver_id", "is_read"]),
        Index(value = ["deleted_at"])
    ]
)
data class PrivateMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "sender_id")
    val senderId: Long,
    @ColumnInfo(name = "receiver_id")
    val receiverId: Long,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)


// 时间转换器
//object TimestampConverter {
//    @TypeConverter
//    fun fromTimestamp(value: Long?): Date? {
//        return if (value == null) null else Date(value)
//    }
//
//    @TypeConverter
//    fun dateToTimestamp(date: Date?): Long? {
//        return date?.time
//    }
//}
