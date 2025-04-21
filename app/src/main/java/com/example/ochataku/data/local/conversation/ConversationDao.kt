package com.example.ochataku.data.local.conversation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Query(
        """
    SELECT * FROM conversation 
    WHERE isGroup = 1 
       OR aId = :userId 
       OR bId = :userId 
    ORDER BY timestamp DESC
"""
    )
    suspend fun getConversationsForUser(userId: Long): List<ConversationEntity>

    @Query(
        """
    DELETE FROM conversation 
    WHERE isGroup = 1 
       OR aId = :userId 
       OR bId = :userId
"""
    )
    suspend fun clearConversationsForUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ConversationEntity>)

    @Query("DELETE FROM conversation WHERE convId = :conversationId")
    suspend fun deleteConversation(conversationId: Long)


    @Query(
        """
    SELECT c.convId AS convId,
           c.aId AS aId,
           c.bId AS bId,
           c.groupId AS groupId,
           c.isGroup AS isGroup,
           CASE WHEN c.isGroup = 1 
                THEN g.group_name 
                ELSE u.username 
           END AS name,
           CASE WHEN c.isGroup = 1 
                THEN g.avatar 
                ELSE u.avatar 
           END AS avatar,
           c.lastMessage AS lastMessage,
           c.timestamp AS timestamp
    FROM conversation c
    LEFT JOIN `group` g ON c.groupId = g.group_id AND c.isGroup = 1
    LEFT JOIN users u 
      ON (c.aId = u.user_id AND c.bId = :userId AND c.isGroup = 0)
      OR (c.bId = u.user_id AND c.aId = :userId AND c.isGroup = 0)
    WHERE (c.aId = :userId OR c.bId = :userId OR c.isGroup = 1)
    ORDER BY c.timestamp DESC
"""
    )
    suspend fun getDisplayConversations(userId: Long): List<ConversationDisplay>

}
