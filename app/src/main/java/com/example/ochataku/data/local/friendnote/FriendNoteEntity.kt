package com.example.ochataku.data.local.friendnote

import androidx.room.*
import com.example.ochataku.data.local.user.UserEntity

@Entity(
    tableName = "friend_notes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "friend_id"], unique = true)
    ]
)
data class FriendNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "friend_id")
    val friendId: Long,
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: Long = System.currentTimeMillis()
)