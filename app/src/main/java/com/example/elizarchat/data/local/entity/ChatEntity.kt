package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity чата для хранения в Room Database.
 * Соответствует таблице 'chats' в PostgreSQL сервера.
 */
@Entity(tableName = "chats")
@TypeConverters(InstantConverter::class)
data class ChatEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,  // "private", "group", "channel"

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "created_by")
    val createdBy: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,

    @ColumnInfo(name = "last_message_id")
    val lastMessageId: String? = null,

    // === ЛОКАЛЬНЫЕ ПОЛЯ (только для клиента) ===
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Instant = Instant.now(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, DIRTY
)