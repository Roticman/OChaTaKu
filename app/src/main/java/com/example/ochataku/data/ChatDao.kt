package com.example.ochataku.data

import androidx.room.Dao
import androidx.room.Query
import com.example.ochataku.model.ChatItem

@Dao
interface ChatDao {
    // 获取私聊列表
    @Query("""
        SELECT u.id, u.username AS name, u.avatar_url, pm.content AS lastMessage, pm.created_at AS lastMessageTime
        FROM private_messages pm
        JOIN users u ON (pm.sender_id = u.id OR pm.receiver_id = u.id)
        WHERE pm.sender_id = :userId OR pm.receiver_id = :userId
        GROUP BY u.id
        ORDER BY pm.created_at DESC
    """)
    suspend fun getPrivateChats(userId: Long): List<ChatItem>

    // 获取群聊列表（用户加入的，并且有消息记录的）
    @Query("""
        SELECT g.id, g.name, NULL AS avatarUrl, gm.content AS lastMessage, gm.created_at AS lastMessageTime
        FROM groups g
        JOIN group_members gmbr ON g.id = gmbr.group_id
        JOIN group_messages gm ON g.id = gm.group_id
        WHERE gmbr.user_id = :userId
        GROUP BY g.id
        ORDER BY gm.created_at DESC
    """)
    suspend fun getGroupChats(userId: Long): List<ChatItem>
}
