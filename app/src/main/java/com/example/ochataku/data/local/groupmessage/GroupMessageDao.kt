package com.example.ochataku.data.local.groupmessage

import androidx.room.*

@Dao
interface GroupMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GroupMessageEntity): Long

    @Query("SELECT * FROM group_messages WHERE group_id = :groupId ORDER BY created_at ASC")
    suspend fun getMessagesByGroupId(groupId: Long): List<GroupMessageEntity>

    @Query("UPDATE group_messages SET deleted_at = CURRENT_TIMESTAMP WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: Long)
}