package com.example.ochataku.data.local.groupmember

import androidx.room.*
import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.data.local.group.GroupEntity

@Entity(
    tableName = "group_members",
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
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["group_id", "user_id"], unique = true)
    ]
)
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "group_id")
    val groupId: Long,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "joined_at", defaultValue = "CURRENT_TIMESTAMP")
    val joinedAt: Long = System.currentTimeMillis()
)