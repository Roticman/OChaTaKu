// MessageDao.kt
package com.example.ochataku.data.local.message

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    /** 批量插入或替换（网络拉取后写入本地） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    /** 单条插入 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    /** 通过会话ID查询消息，按时间升序 */
    @Query("SELECT * FROM message WHERE conv_id = :convId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(convId: Long): List<MessageEntity>

    /** 清空所有消息（如需） */
    @Query("DELETE FROM message")
    suspend fun clearAll()
}
