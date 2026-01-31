package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.MessageEntity
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.domain.model.Message
import com.example.elizarchat.domain.model.MessageStatus
import com.example.elizarchat.domain.model.SyncStatus
import java.time.Instant
import java.time.format.DateTimeParseException

object MessageMapper {

    // === DTO → Entity ===
    fun dtoToEntity(dto: MessageDto): MessageEntity {
        return MessageEntity(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = dto.id,
            chatId = dto.chatId,  // Исправлено: chat_id → chatId
            senderId = dto.senderId,  // Исправлено: sender_id → senderId
            content = dto.content,
            type = dto.type,
            metadata = dto.metadata,
            replyTo = dto.replyTo,  // Исправлено: reply_to → replyTo
            status = dto.status,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),  // Исправлено: created_at → createdAt
            updatedAt = parseInstant(dto.updatedAt),  // Исправлено: updated_at → updatedAt
            deletedAt = parseInstant(dto.deletedAt),  // Исправлено: deleted_at → deletedAt
            readBy = dto.readBy ?: emptyList(),  // Исправлено: read_by → readBy

            // ЛОКАЛЬНЫЕ ПОЛЯ (по умолчанию)
            localStatus = parseLocalStatus(dto.status),
            isSending = false,
            localId = null,
            syncStatus = "SYNCED"
        )
    }

    // === Entity → Domain ===
    fun entityToDomain(entity: MessageEntity): Message {
        return Message(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = entity.id.toString(),
            chatId = entity.chatId.toString(),
            senderId = entity.senderId.toString(),
            content = entity.content,
            type = entity.type,
            metadata = entity.metadata,
            replyTo = entity.replyTo?.toString(),
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
            readBy = entity.readBy.map { it.toString() },

            // ЛОКАЛЬНЫЕ ПОЛЯ
            localStatus = parseMessageStatus(entity.localStatus),
            isSending = entity.isSending,
            localId = entity.localId,
            syncStatus = parseSyncStatus(entity.syncStatus)
        )
    }

    // === DTO → Domain ===
    fun dtoToDomain(dto: MessageDto): Message {
        return Message(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = dto.id.toString(),
            chatId = dto.chatId.toString(),  // Исправлено
            senderId = dto.senderId.toString(),  // Исправлено
            content = dto.content,
            type = dto.type,
            metadata = dto.metadata,
            replyTo = dto.replyTo?.toString(),  // Исправлено
            status = dto.status,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),  // Исправлено
            updatedAt = parseInstant(dto.updatedAt),  // Исправлено
            deletedAt = parseInstant(dto.deletedAt),  // Исправлено
            readBy = dto.readBy?.map { it.toString() } ?: emptyList(),  // Исправлено

            // ЛОКАЛЬНЫЕ ПОЛЯ (по умолчанию)
            localStatus = parseMessageStatus(parseLocalStatus(dto.status)),
            isSending = false,
            localId = null,
            syncStatus = SyncStatus.SYNCED
        )
    }

    // === Domain → Entity (для отправки сообщений) ===
    fun domainToEntity(domain: Message): MessageEntity {
        return MessageEntity(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = domain.id.toIntOrNull() ?: 0,
            chatId = domain.chatId.toIntOrNull() ?: 0,
            senderId = domain.senderId.toIntOrNull() ?: 0,
            content = domain.content,
            type = domain.type,
            metadata = domain.metadata,
            replyTo = domain.replyTo?.toIntOrNull(),
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
            readBy = domain.readBy.mapNotNull { it.toIntOrNull() },

            // ЛОКАЛЬНЫЕ ПОЛЯ
            localStatus = domain.localStatus.toString(),
            isSending = domain.isSending,
            localId = domain.localId,
            syncStatus = domain.syncStatus.toString()
        )
    }

    // === Вспомогательные методы ===
    private fun parseMessageStatus(statusString: String): MessageStatus {
        return when (statusString.lowercase()) {
            "sending" -> MessageStatus.SENDING
            "sent" -> MessageStatus.SENT
            "delivered" -> MessageStatus.DELIVERED
            "read" -> MessageStatus.READ
            "error" -> MessageStatus.ERROR
            else -> MessageStatus.SENT
        }
    }

    private fun parseLocalStatus(serverStatus: String?): String {
        return when (serverStatus?.lowercase()) {
            "sending" -> "sending"
            "sent" -> "sent"
            "delivered" -> "delivered"
            "read" -> "read"
            else -> "sent"
        }
    }

    private fun parseSyncStatus(status: String): SyncStatus {
        return when (status.uppercase()) {
            "SYNCED" -> SyncStatus.SYNCED
            "PENDING_SEND" -> SyncStatus.PENDING_SEND
            "PENDING_EDIT" -> SyncStatus.PENDING_EDIT
            "PENDING_DELETE" -> SyncStatus.PENDING_DELETE
            else -> SyncStatus.SYNCED
        }
    }

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // === Для массовой конвертации ===
    fun dtosToEntities(dtos: List<MessageDto>): List<MessageEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun entitiesToDomains(entities: List<MessageEntity>): List<Message> {
        return entities.map { entityToDomain(it) }
    }

    fun dtosToDomains(dtos: List<MessageDto>): List<Message> {
        return dtos.map { dtoToDomain(it) }
    }
}