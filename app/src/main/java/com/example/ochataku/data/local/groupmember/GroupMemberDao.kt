package com.example.ochataku.data.local.groupmember

import androidx.room.*

@Dao
interface GroupMemberDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMember(groupMember: GroupMemberEntity): Long

    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    suspend fun getMembersByGroupId(groupId: Long): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE user_id = :userId")
    suspend fun getGroupsByUserId(userId: Long): List<GroupMemberEntity>

    @Query("DELETE FROM group_members WHERE group_id = :groupId AND user_id = :userId")
    suspend fun removeMember(groupId: Long, userId: Long)
}