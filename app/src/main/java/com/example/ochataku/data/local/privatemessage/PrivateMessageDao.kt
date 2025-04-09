package com.example.ochataku.data.local.privatemessage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivateMessageDao {
    // 插入新消息（冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PrivateMessageEntity): Long

    // 获取某两位用户的聊天记录（Flow -> 可观察）
    @Query(
        """
        SELECT * FROM private_messages 
        WHERE (sender_id = :user1 AND receiver_id = :user2) 
           OR (sender_id = :user2 AND receiver_id = :user1) 
        ORDER BY created_at ASC
    """
    )
    fun getChatHistory(user1: Long, user2: Long): Flow<List<PrivateMessageEntity>> // ✅ Flow 监听聊天记录

    // 获取未读消息（Flow -> 可观察）
    @Query("SELECT * FROM private_messages WHERE receiver_id = :receiverId AND is_read = 0")
    fun getUnreadMessages(receiverId: Long): Flow<List<PrivateMessageEntity>> // ✅ 监听未读消息变化

    // 标记消息为已读
    @Query("UPDATE private_messages SET is_read = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Long)

    // 软删除消息（打上时间戳）
    @Query("UPDATE private_messages SET deleted_at = CURRENT_TIMESTAMP WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: Long)
}
