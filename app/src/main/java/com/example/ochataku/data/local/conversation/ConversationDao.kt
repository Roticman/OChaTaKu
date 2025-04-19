package com.example.ochataku.data.local.conversation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getConversationsForUser(userId: Long): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversation WHERE convId = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM conversation WHERE userId = :userId")
    suspend fun clearConversationsForUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ConversationEntity>)

    @Query("""
    SELECT c.convId AS convId,
           c.peerId AS peerId,
           c.isGroup AS isGroup,
           c.lastMessage AS lastMessage,
           c.timestamp AS timestamp,
           CASE WHEN c.isGroup = 1 
                THEN g.group_name 
                ELSE u.username 
           END AS name,
           CASE WHEN c.isGroup = 1 
                THEN g.avatar 
                ELSE u.avatar 
           END AS avatar
    FROM conversation c
    LEFT JOIN `group` g ON c.peerId = g.group_id AND c.isGroup = 1
    LEFT JOIN users u ON c.peerId = u.user_id AND c.isGroup = 0
    WHERE c.userId = :userId
    ORDER BY c.timestamp DESC
""")
    suspend fun getDisplayConversations(userId: Long): List<ConversationDisplay>
}
