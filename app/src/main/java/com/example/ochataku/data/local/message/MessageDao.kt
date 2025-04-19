package com.example.ochataku.data.local.message

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    // ✅ 通过会话ID查询消息（兼容私聊+群聊）
    @Query("SELECT * FROM message WHERE conv_id = :convId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(convId: Long): List<MessageEntity>

    @Query("DELETE FROM message")
    suspend fun clearAll()
}
