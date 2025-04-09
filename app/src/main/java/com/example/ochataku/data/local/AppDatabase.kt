package com.example.ochataku.data.local

import androidx.room.*;
import com.example.ochataku.data.local.friend.FriendDao
import com.example.ochataku.data.local.friend.FriendEntity
import com.example.ochataku.data.local.friendnote.FriendNoteDao
import com.example.ochataku.data.local.friendnote.FriendNoteEntity
import com.example.ochataku.data.local.group.GroupDao
import com.example.ochataku.data.local.group.GroupEntity
import com.example.ochataku.data.local.groupmember.GroupMemberDao
import com.example.ochataku.data.local.groupmember.GroupMemberEntity
import com.example.ochataku.data.local.groupmessage.GroupMessageDao
import com.example.ochataku.data.local.groupmessage.GroupMessageEntity
import com.example.ochataku.data.local.privatemessage.PrivateMessageDao
import com.example.ochataku.data.local.privatemessage.PrivateMessageEntity
import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.data.local.user.UserEntity

@Database(
    entities = [UserEntity::class, PrivateMessageEntity::class, GroupEntity::class, GroupMemberEntity::class, GroupMessageEntity::class, FriendEntity::class, FriendNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun privateMessageDao(): PrivateMessageDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun friendDao(): FriendDao
    abstract fun friendNoteDao(): FriendNoteDao

    //companion object 让 INSTANCE 变成 全局唯一的数据库实例（单例模式）。
    companion object {

        //@Volatile 确保多个线程同时访问 INSTANCE 时，变量的修改是可见的，避免并发问题。
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //getDatabase(context) 是一个获取 UserDatabase 实例的静态方法。
        fun getDatabase(context: android.content.Context): AppDatabase {

            //synchronized(this) 确保多个线程不会同时创建多个数据库实例（线程安全）。
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(

                    //context.applicationContext：使用应用级的 context，防止内存泄漏。
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}