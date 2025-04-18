package com.example.ochataku.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getConversationsForUser(userId: Long): List<Conversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("DELETE FROM conversation WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM conversation WHERE userId = :userId")
    suspend fun clearConversationsForUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Conversation>)

}