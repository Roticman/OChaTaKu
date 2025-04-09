package com.example.ochataku.data.local.friend

import androidx.room.*

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFriend(friend: FriendEntity): Long

    @Query("SELECT * FROM friends WHERE user_id = :userId")
    suspend fun getFriendsByUserId(userId: Long): List<FriendEntity>

    @Query("UPDATE friends SET status = :status WHERE user_id = :userId AND friend_id = :friendId")
    suspend fun updateFriendStatus(userId: Long, friendId: Long, status: String)
}
