package com.example.ochataku.data.local.groupmessage

import androidx.room.*
import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.data.local.group.GroupEntity

@Entity(
    tableName = "group_messages",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["group_id", "created_at"]),
        Index(value = ["deleted_at"])
    ]
)
data class GroupMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "group_id")
    val groupId: Long,
    @ColumnInfo(name = "sender_id")
    val senderId: Long,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)