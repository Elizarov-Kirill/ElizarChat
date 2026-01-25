package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.MessageEntity
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.domain.model.Message
import com.example.elizarchat.domain.model.MessageStatus
import com.example.elizarchat.domain.model.MessageType
import java.time.Instant
import java.time.format.DateTimeParseException

object MessageMapper {

    // === DTO → Entity ===
    fun dtoToEntity(dto: MessageDto): MessageEntity {
        return MessageEntity(
            id = dto.id,
            chatId = dto.chatId,
            userId = dto.userId,
            content = dto.content,
            messageType = dto.messageType,
            metadata = dto.metadata,
            isEdited = dto.isEdited,
            isDeleted = dto.isDeleted,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            // Локальные поля по умолчанию
            status = "sent",
            isSending = false,
            isFailed = false,
            localId = null,
            replyTo = null,
            syncStatus = "SYNCED"
        )
    }

    // === Entity → Domain ===
    fun entityToDomain(entity: MessageEntity): Message {
        return Message(
            id = entity.id,
            chatId = entity.chatId,
            userId = entity.userId,
            content = entity.content,
            messageType = parseMessageType(entity.messageType),
            metadata = entity.metadata,
            isEdited = entity.isEdited,
            isDeleted = entity.isDeleted,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            // Локальные поля
            status = parseMessageStatus(entity.status),
            isSending = entity.isSending,
            isFailed = entity.isFailed,
            localId = entity.localId,
            replyTo = entity.replyTo
        )
    }

    // === DTO → Domain ===
    fun dtoToDomain(dto: MessageDto): Message {
        return Message(
            id = dto.id,
            chatId = dto.chatId,
            userId = dto.userId,
            content = dto.content,
            messageType = parseMessageType(dto.messageType),
            metadata = dto.metadata,
            isEdited = dto.isEdited,
            isDeleted = dto.isDeleted,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            // Локальные поля по умолчанию
            status = MessageStatus.SENT,
            isSending = false,
            isFailed = false,
            localId = null,
            replyTo = null
        )
    }

    // === Domain → Entity ===
    fun domainToEntity(domain: Message): MessageEntity {
        return MessageEntity(
            id = domain.id,
            chatId = domain.chatId,
            userId = domain.userId,
            content = domain.content,
            messageType = domain.messageType.name.lowercase(),
            metadata = domain.metadata,
            isEdited = domain.isEdited,
            isDeleted = domain.isDeleted,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            // Локальные поля
            status = domain.status.name.lowercase(),
            isSending = domain.isSending,
            isFailed = domain.isFailed,
            localId = domain.localId,
            replyTo = domain.replyTo,
            syncStatus = when {
                domain.isSending -> "PENDING_SEND"
                domain.status == MessageStatus.ERROR -> "PENDING_SEND"
                else -> "SYNCED"
            }
        )
    }

    // === Вспомогательные методы ===

    private fun parseMessageType(typeString: String): MessageType {
        return when (typeString.lowercase()) {
            "text" -> MessageType.TEXT
            "image" -> MessageType.IMAGE
            "video" -> MessageType.VIDEO
            "file" -> MessageType.FILE
            "voice" -> MessageType.VOICE
            "system" -> MessageType.SYSTEM
            else -> MessageType.TEXT
        }
    }

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

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // === Пакетные преобразования ===

    fun dtosToEntities(dtos: List<MessageDto>): List<MessageEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun entitiesToDomains(entities: List<MessageEntity>): List<Message> {
        return entities.map { entityToDomain(it) }
    }

    fun dtosToDomains(dtos: List<MessageDto>): List<Message> {
        return dtos.map { dtoToDomain(it) }
    }

    fun domainsToEntities(domains: List<Message>): List<MessageEntity> {
        return domains.map { domainToEntity(it) }
    }

    // === Создание временного сообщения ===

    fun createTemporaryMessage(
        localId: String,
        chatId: String,
        userId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        metadata: String? = null,
        replyTo: String? = null
    ): Pair<Message, MessageEntity> {
        val now = Instant.now()

        val message = Message(
            id = localId,
            chatId = chatId,
            userId = userId,
            content = content,
            messageType = messageType,
            metadata = metadata,
            createdAt = now,
            status = MessageStatus.SENDING,
            isSending = true,
            localId = localId,
            replyTo = replyTo
        )

        val entity = MessageEntity(
            id = localId,
            chatId = chatId,
            userId = userId,
            content = content,
            messageType = messageType.name.lowercase(),
            metadata = metadata,
            createdAt = now,
            status = "sending",
            isSending = true,
            localId = localId,
            replyTo = replyTo,
            syncStatus = "PENDING_SEND"
        )

        return Pair(message, entity)
    }
}