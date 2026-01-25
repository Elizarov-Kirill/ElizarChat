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
        Index(value = ["chat_id"])
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
        )
    ]
)
@TypeConverters(InstantConverter::class)
data class ChatMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "chat_id")
    val chatId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "role")
    val role: String,  // "owner", "admin", "member", "guest"

    @ColumnInfo(name = "joined_at")
    val joinedAt: Instant,

    @ColumnInfo(name = "last_read_message_id")
    val lastReadMessageId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null
)