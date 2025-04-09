package com.example.ochataku.data.local.user
import androidx.room.*


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Long = System.currentTimeMillis()
)




//INSTANCE ?: synchronized(this) {}
//如果 INSTANCE 为空，则创建数据库实例。

//Room.databaseBuilder()

//UserDatabase::class.java：指定数据库的 class。
//"user_database"：数据库文件的名称（存储在 data/data/your.package.name/databases/ 目录）。
//.build()：构建数据库，不启用数据库迁移（如果版本变更会导致数据丢失）。