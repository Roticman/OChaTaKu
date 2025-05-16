package com.example.ochataku.data.local.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "group")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "group_id")
    @SerializedName("group_id")
    val groupId: Long,

    @ColumnInfo(name = "group_name")
    @SerializedName("group_name")
    val groupName: String,

    @ColumnInfo(name = "avatar")
    @SerializedName("avatar")
    val avatar: String? = null,

    @ColumnInfo(name = "announcement")
    val announcement: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "creator_id")
    @SerializedName("creator_id")
    val creatorId: Long,

    @ColumnInfo(name = "member_count")
    @SerializedName("member_count")
    val memberCount: Int = 1,

    @ColumnInfo(name = "max_member_count")
    @SerializedName("max_member_count")
    val maxMemberCount: Int = 500,
)
