package com.example.ochataku.data.local.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    val groupId: Long,

    @ColumnInfo(name = "group_name")
    val groupName: String,

    @ColumnInfo(name = "avatar")
    val avatar: String? = null,

    @ColumnInfo(name = "announcement")
    val announcement: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "creator_id")
    val creatorId: Long,

    @ColumnInfo(name = "member_count")
    val memberCount: Int = 1,

    @ColumnInfo(name = "max_member_count")
    val maxMemberCount: Int = 500,

    @ColumnInfo(name = "type")
    val type: Int = 1, // 1=普通群，2=工作群，3=公开群

    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false,

    @ColumnInfo(name = "is_dismissed")
    val isDismissed: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)
