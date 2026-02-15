package com.example.elizarchat.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.elizarchat.data.local.converter.*
import com.example.elizarchat.data.local.dao.ChatDao
import com.example.elizarchat.data.local.dao.ChatMemberDao
import com.example.elizarchat.data.local.dao.MessageDao
import com.example.elizarchat.data.local.dao.UserDao
import com.example.elizarchat.data.local.entity.ChatEntity
import com.example.elizarchat.data.local.entity.ChatMemberEntity
import com.example.elizarchat.data.local.entity.MessageEntity
import com.example.elizarchat.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        ChatMemberEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    InstantConverter::class,
    StringListConverter::class,
    IntListConverter::class,
    JsonObjectConverter::class  // Добавлен новый конвертер
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun chatMemberDao(): ChatMemberDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elizarchat.db"
                )
                    .addMigrations()
                    .fallbackToDestructiveMigration() // для разработки
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}