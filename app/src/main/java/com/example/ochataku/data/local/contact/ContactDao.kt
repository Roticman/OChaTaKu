package com.example.ochataku.data.local.contact

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("SELECT * FROM contact ORDER BY remark_name COLLATE NOCASE ASC")
    suspend fun getAllContacts(): List<ContactEntity>

    @Query("DELETE FROM contact")
    suspend fun clearContacts()

}
