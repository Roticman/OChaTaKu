package com.example.ochataku.data.local.user
import androidx.room.*

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], name = "idx_username"),
        Index(value = ["phone"], name = "idx_phone"),
        Index(value = ["email"], name = "idx_email")
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "username")
    val username: String? = null,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "avatar")
    val avatar: String? = null,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "gender")
    val gender: Int? = null, // 0=未知，1=男，2=女

    @ColumnInfo(name = "region")
    val region: String? = null,

    @ColumnInfo(name = "signature")
    val signature: String? = null,

    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,
)