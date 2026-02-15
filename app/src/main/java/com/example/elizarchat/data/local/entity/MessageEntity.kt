package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.*
import kotlinx.serialization.json.JsonObject
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
@TypeConverters(
    InstantConverter::class,
    IntListConverter::class,
    JsonObjectConverter::class  // Добавлен конвертер для JsonObject
)
data class MessageEntity(
    // ============ СЕРВЕРНЫЕ ПОЛЯ ============
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "chat_id")
    val chatId: Int?,

    @ColumnInfo(name = "sender_id")
    val senderId: Int?,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "metadata")
    val metadata: JsonObject,  // Теперь будет работать с конвертером

    @ColumnInfo(name = "reply_to")
    val replyTo: Int? = null,

    @ColumnInfo(name = "status")
    val status: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null,

    @ColumnInfo(name = "read_by")
    val readBy: List<Int> = emptyList(),

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ ============
    @ColumnInfo(name = "local_status")
    val localStatus: String = "sending",

    @ColumnInfo(name = "is_sending")
    val isSending: Boolean = false,

    @ColumnInfo(name = "local_id")
    val localId: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"
)