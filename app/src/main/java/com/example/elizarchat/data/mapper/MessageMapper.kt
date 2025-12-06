package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.MessageEntity
import com.example.elizarchat.data.remote.dto.AttachmentDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.domain.model.*
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Маппер для преобразований Message.
 */
object MessageMapper {

    // === Основные преобразования ===

    fun dtoToDomain(
        dto: MessageDto,
        sender: User? = null,
        chat: Chat? = null,
        replyToMessage: Message? = null
    ): Message {
        val replyPreview = replyToMessage?.let {
            MessagePreview(
                id = it.id,
                content = it.previewContent,
                senderId = it.senderId,
                senderName = it.sender?.displayNameOrUsername ?: "Unknown",
                timestamp = it.createdAt,
                type = it.type,
                status = it.status
            )
        }

        return Message(
            id = dto.id.toString(), // Long → String
            chatId = dto.chatId.toString(), // Long → String
            senderId = dto.senderId.toString(), // Long → String
            content = dto.content,
            type = parseMessageType(dto.type),
            status = parseMessageStatus(dto.status),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            sender = sender,
            chat = chat,
            attachments = dto.attachments?.map { attachmentDtoToDomain(it) } ?: emptyList(),
            replyTo = replyPreview,
            readBy = dto.readBy.map { it.toString() }
        )
    }

    fun dtoToEntity(dto: MessageDto): MessageEntity {
        return MessageEntity(
            id = dto.id.toString(),
            chatId = dto.chatId.toString(),
            senderId = dto.senderId.toString(),
            content = dto.content,
            type = parseMessageType(dto.type),
            status = parseMessageStatus(dto.status),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            attachmentsJson = null, // Пока не обрабатываем вложения
            replyTo = dto.replyTo?.toString(),
            readBy = dto.readBy, // List<Long>
            syncStatus = MessageEntity.SyncStatus.SYNCED
        )
    }

    fun entityToDomain(
        entity: MessageEntity,
        sender: User? = null,
        chat: Chat? = null,
        replyToMessage: Message? = null
    ): Message {
        val attachments = emptyList<Attachment>() // Пока пусто, т.к. не обрабатываем вложения

        val replyPreview = replyToMessage?.let {
            MessagePreview(
                id = it.id,
                content = it.previewContent,
                senderId = it.senderId,
                senderName = it.sender?.displayNameOrUsername ?: "Unknown",
                timestamp = it.createdAt,
                type = it.type,
                status = it.status
            )
        }

        return Message(
            id = entity.id,
            chatId = entity.chatId,
            senderId = entity.senderId,
            content = entity.content,
            type = entity.type,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            sender = sender,
            chat = chat,
            attachments = attachments,
            replyTo = replyPreview,
            readBy = entity.readBy.map { it.toString() }, // Long → String
            isSending = entity.isSending,
            isFailed = entity.isFailed,
            localId = entity.localId
        )
    }

    fun domainToEntity(
        domain: Message,
        syncStatus: MessageEntity.SyncStatus = MessageEntity.SyncStatus.SYNCED
    ): MessageEntity {
        return MessageEntity(
            id = domain.id,
            chatId = domain.chatId,
            senderId = domain.senderId,
            content = domain.content,
            type = domain.type,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            attachmentsJson = null, // Пока не обрабатываем
            replyTo = domain.replyTo?.id,
            readBy = domain.readBy.map { it.toLongOrNull() ?: 0L }.filter { it > 0 },
            isSending = domain.isSending,
            isFailed = domain.isFailed,
            localId = domain.localId,
            syncStatus = syncStatus
        )
    }

    // === Вспомогательные методы ===

    private fun attachmentDtoToDomain(dto: AttachmentDto): Attachment {
        return Attachment(
            id = dto.id,
            url = dto.url,
            type = parseAttachmentType(dto.type),
            name = dto.name,
            size = dto.size,
            duration = dto.duration,
            thumbnailUrl = dto.thumbnailUrl,
            width = dto.width,
            height = dto.height
        )
    }

    private fun parseMessageType(typeString: String): MessageType {
        return when (typeString.lowercase()) {
            "text" -> MessageType.TEXT
            "image" -> MessageType.IMAGE
            "video" -> MessageType.VIDEO
            "audio" -> MessageType.AUDIO
            "file" -> MessageType.FILE
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
            "failed" -> MessageStatus.FAILED
            "deleted" -> MessageStatus.DELETED
            else -> MessageStatus.SENT
        }
    }

    private fun parseAttachmentType(typeString: String): AttachmentType {
        return when (typeString.lowercase()) {
            "image" -> AttachmentType.IMAGE
            "video" -> AttachmentType.VIDEO
            "audio" -> AttachmentType.AUDIO
            else -> AttachmentType.FILE
        }
    }

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // === Для работы со списками ===

    fun dtosToDomains(
        dtos: List<MessageDto>,
        sendersMap: Map<Long, User> = emptyMap(),
        chatsMap: Map<Long, Chat> = emptyMap(),
        repliesMap: Map<Long, Message> = emptyMap()
    ): List<Message> {
        return dtos.map { dto ->
            dtoToDomain(
                dto = dto,
                sender = sendersMap[dto.senderId],
                chat = chatsMap[dto.chatId],
                replyToMessage = dto.replyTo?.let { repliesMap[it] }
            )
        }
    }

    fun entitiesToDomains(
        entities: List<MessageEntity>,
        sendersMap: Map<String, User> = emptyMap(),
        chatsMap: Map<String, Chat> = emptyMap(),
        repliesMap: Map<String, Message> = emptyMap()
    ): List<Message> {
        return entities.map { entity ->
            entityToDomain(
                entity = entity,
                sender = sendersMap[entity.senderId],
                chat = chatsMap[entity.chatId],
                replyToMessage = entity.replyTo?.let { repliesMap[it] }
            )
        }
    }

    fun dtosToEntities(dtos: List<MessageDto>): List<MessageEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    // === Для создания временных сообщений ===

    fun createLocalMessage(
        chatId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        localId: String = "local_${System.currentTimeMillis()}"
    ): Message {
        return Message(
            id = localId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            type = type,
            status = MessageStatus.SENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isSending = true,
            localId = localId
        )
    }

    // === Обновление статуса ===

    fun updateMessageStatus(
        message: Message,
        status: MessageStatus
    ): Message {
        return message.copy(
            status = status,
            updatedAt = Instant.now(),
            isSending = status == MessageStatus.SENDING,
            isFailed = status == MessageStatus.FAILED
        )
    }
}