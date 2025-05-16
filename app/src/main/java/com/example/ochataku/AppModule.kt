package com.example.ochataku

import android.content.Context
import androidx.room.Room
import com.example.ochataku.data.local.AppDatabase
import com.example.ochataku.data.local.contact.ContactDao
import com.example.ochataku.data.local.conversation.ConversationDao
import com.example.ochataku.data.local.group.GroupDao
import com.example.ochataku.data.local.message.MessageDao
import com.example.ochataku.data.local.user.UserDao
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiClient.apiService

//    @Provides
//    fun provideUserRepository(): UserRepository {
//        return UserRepository()
//    }

    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext context: Context): AuthManager {
        return AuthManager(context)
    }

    // DAO 提供方法
    @Provides
    fun provideUserDao(db: AppDatabase): UserDao {
        return db.userDao()
    }


    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao {
        return db.groupDao()
    }
//
//    @Provides
//    fun provideGroupMemberDao(db: AppDatabase): GroupMemberDao {
//        return db.groupMemberDao()
//    }
//
    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao {
        return db.MessageDao()
    }

    //
//    @Provides
//    fun provideFriendDao(db: AppDatabase): FriendDao {
//        return db.friendDao()
//    }
//
//    @Provides
//    fun provideFriendNoteDao(db: AppDatabase): FriendNoteDao {
//        return db.friendNoteDao()
//    }
//
//
    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao {
        return db.ConversationDao()
    }

    @Provides
    fun provideContactDao(db: AppDatabase): ContactDao {
        return db.ContactDao()
    }
}