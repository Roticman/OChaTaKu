package com.example.ochataku.data.local.group

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Query("SELECT * FROM `group`")
    suspend fun getGroups(): List<GroupEntity>?

    @Query("DELETE FROM `group`")
    suspend fun clearAllGroups()
}
