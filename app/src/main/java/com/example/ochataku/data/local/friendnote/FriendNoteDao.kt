package com.example.ochataku.data.local.friendnote

import androidx.room.*

@Dao
interface FriendNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateNote(note: FriendNoteEntity): Long

    @Query("SELECT * FROM friend_notes WHERE user_id = :userId AND friend_id = :friendId")
    suspend fun getNoteByUserAndFriend(userId: Long, friendId: Long): FriendNoteEntity?
}