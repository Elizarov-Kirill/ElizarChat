package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity чата для хранения в Room Database.
 * Соответствует таблице 'chats' в PostgreSQL сервера.
 */
@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["type"]),
        Index(value = ["created_by"]),
        Index(value = ["last_message_at"]),
        Index(value = ["updated_at"])
    ]
)
@TypeConverters(InstantConverter::class)
data class ChatEntity(
    // ============ СЕРВЕРНЫЕ ПОЛЯ (синхронизированы с БД) ============
    @PrimaryKey
    val id: Int,  // INTEGER PRIMARY KEY

    @ColumnInfo(name = "type")
    val type: String,  // VARCHAR(20): 'private', 'group', 'channel'

    @ColumnInfo(name = "name")
    val name: String? = null,  // VARCHAR(255)

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,  // TEXT

    @ColumnInfo(name = "created_by")
    val createdBy: Int,  // INTEGER REFERENCES users(id)

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,  // TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,  // TIMESTAMP

    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: Instant? = null,  // TIMESTAMP (на сервере)

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ (только для клиента) ============
    @ColumnInfo(name = "last_message_id")
    val lastMessageId: Int? = null,  // Локальная ссылка на последнее сообщение

    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Instant = Instant.now(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED",  // SYNCED, PENDING, DIRTY

    // ВНИМАНИЕ: unread_count перемещен в ChatMemberEntity!
    // Каждый участник чата имеет свой собственный счетчик непрочитанных
)