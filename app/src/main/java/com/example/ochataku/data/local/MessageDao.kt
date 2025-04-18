
// ✅ MessageDao.kt
package com.example.ochataku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM message WHERE (receiver_id = :peerId AND is_group = :isGroup) ORDER BY timestamp ASC")
    suspend fun getMessagesForPeer(peerId: Long, isGroup: Boolean): List<MessageEntity>

    @Query("DELETE FROM message")
    suspend fun clearAll()
}
