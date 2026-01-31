package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity участника чата для связи многие-ко-многим.
 * Соответствует таблице 'chat_members' в PostgreSQL сервера.
 */
@Entity(
    tableName = "chat_members",
    indices = [
        Index(value = ["chat_id", "user_id"], unique = true),
        Index(value = ["user_id"]),
        Index(value = ["chat_id"]),
        Index(value = ["role"]),
        Index(value = ["joined_at"]),
        Index(value = ["last_read_message_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["last_read_message_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@TypeConverters(InstantConverter::class)
data class ChatMemberEntity(
    // ============ СЕРВЕРНЫЕ ПОЛЯ (синхронизированы с БД) ============
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,  // SERIAL PRIMARY KEY (PostgreSQL) - изменено с Long на Int!

    @ColumnInfo(name = "chat_id")
    val chatId: Int,  // INTEGER REFERENCES chats(id)

    @ColumnInfo(name = "user_id")
    val userId: Int,  // INTEGER REFERENCES users(id)

    @ColumnInfo(name = "role")
    val role: String,  // VARCHAR(20): 'owner', 'admin', 'member', 'guest'

    @ColumnInfo(name = "joined_at")
    val joinedAt: Instant,  // TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,  // INTEGER DEFAULT 0 (добавлено!)

    @ColumnInfo(name = "last_read_message_id")
    val lastReadMessageId: Int? = null,  // INTEGER REFERENCES messages(id)

    // ============ ЛОКАЛЬНЫЕ/СЛУЖЕБНЫЕ ПОЛЯ ============
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true,

    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false,

    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Instant = Instant.now(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING, DIRTY
)