package com.example.ochataku.data.local.group
import androidx.room.*

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)
}