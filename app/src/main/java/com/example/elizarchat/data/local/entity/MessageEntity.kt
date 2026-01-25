package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity для хранения сообщений в Room.
 * Соответствует таблице 'messages' в PostgreSQL сервера.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(InstantConverter::class)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "chat_id")
    val chatId: String,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "message_type")
    val messageType: String,  // "text", "image", "video", "file", "voice", "system"

    @ColumnInfo(name = "metadata")
    val metadata: String? = null,  // JSON строка (заменили attachmentsJson)

    @ColumnInfo(name = "is_edited")
    val isEdited: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,

    // === ЛОКАЛЬНЫЕ ПОЛЯ (только для клиента) ===
    @ColumnInfo(name = "status")
    val status: String = "sending",  // sending, sent, delivered, read, error

    @ColumnInfo(name = "is_sending")
    val isSending: Boolean = false,

    @ColumnInfo(name = "is_failed")
    val isFailed: Boolean = false,

    @ColumnInfo(name = "local_id")
    val localId: String? = null,

    @ColumnInfo(name = "reply_to")
    val replyTo: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SEND, PENDING_EDIT, PENDING_DELETE
)