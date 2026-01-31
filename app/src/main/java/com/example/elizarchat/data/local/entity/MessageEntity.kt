package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import com.example.elizarchat.data.local.converter.IntListConverter
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
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["reply_to"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["chat_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["reply_to"]),
        Index(value = ["created_at"]),
        Index(value = ["chat_id", "created_at"])
    ]
)
@TypeConverters(InstantConverter::class, IntListConverter::class)
data class MessageEntity(
    // ============ СЕРВЕРНЫЕ ПОЛЯ (синхронизированы с БД) ============
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,  // INTEGER PRIMARY KEY

    @ColumnInfo(name = "chat_id")
    val chatId: Int,  // INTEGER REFERENCES chats(id)

    @ColumnInfo(name = "sender_id")
    val senderId: Int,  // INTEGER REFERENCES users(id) - переименовано из userId!

    @ColumnInfo(name = "content")
    val content: String,  // TEXT NOT NULL

    @ColumnInfo(name = "type")
    val type: String,  // VARCHAR(20): 'text', 'image', 'video', 'file', 'voice', 'system'

    @ColumnInfo(name = "metadata")
    val metadata: String? = null,  // JSONB

    @ColumnInfo(name = "reply_to")
    val replyTo: Int? = null,  // INTEGER REFERENCES messages(id)

    @ColumnInfo(name = "status")
    val status: String? = null,  // VARCHAR(20): 'sending', 'sent', 'delivered', 'read'

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,  // TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,  // TIMESTAMP

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null,  // TIMESTAMP (мягкое удаление)

    @ColumnInfo(name = "read_by")
    val readBy: List<Int> = emptyList(),  // INTEGER[] DEFAULT '{}' (массив ID пользователей)

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ (только для клиента) ============
    @ColumnInfo(name = "local_status")
    val localStatus: String = "sending",  // sending, sent, delivered, read, error

    @ColumnInfo(name = "is_sending")
    val isSending: Boolean = false,

    @ColumnInfo(name = "local_id")
    val localId: String? = null,  // Временный ID для сообщений до синхронизации

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SEND, PENDING_EDIT, PENDING_DELETE
)