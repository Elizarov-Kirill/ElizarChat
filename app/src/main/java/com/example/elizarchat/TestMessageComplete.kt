package com.example.elizarchat

import com.example.elizarchat.data.mapper.MessageMapper
import com.example.elizarchat.data.remote.dto.AttachmentDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.domain.model.*
import java.time.Instant

object TestMessageComplete {

    fun runTest() {
        println("\n=== ПОЛНЫЙ ТЕСТ СУЩНОСТИ MESSAGE ===")

        // 1. Тест простого текстового сообщения
        println("1. Тест текстового сообщения:")

        val textMessageDto = MessageDto(
            id = 1001L,
            chatId = 123L,
            senderId = 1L,
            content = "Привет! Как дела?",
            type = "text",
            status = "delivered",
            createdAt = "2024-01-15T14:30:00Z",
            readBy = listOf(1L, 2L)
        )

        val textMessage = MessageMapper.dtoToDomain(textMessageDto)
        println("   - ID: ${textMessage.id}")
        println("   - Chat ID: ${textMessage.chatId}")
        println("   - Sender ID: ${textMessage.senderId}")
        println("   - Content: ${textMessage.content}")
        println("   - Type: ${textMessage.type}")
        println("   - Status: ${textMessage.status}")
        println("   - Created: ${textMessage.createdAt}")
        println("   - Preview: ${textMessage.previewContent}")
        println("   - Can edit: ${textMessage.canEdit}")
        println("   - Can delete: ${textMessage.canDelete}")
        println("   - Is mine (senderId=1): ${textMessage.isMine("1")}")
        println("   - Is mine (senderId=2): ${textMessage.isMine("2")}")

        // 2. Тест сообщения с вложениями
        println("\n2. Тест сообщения с вложениями:")

        val imageAttachment = AttachmentDto(
            id = "attach_001",
            url = "https://example.com/image.jpg",
            type = "image",
            name = "sunset.jpg",
            size = 2048000,
            thumbnailUrl = "https://example.com/thumbnail.jpg",
            width = 1920,
            height = 1080
        )

        val imageMessageDto = MessageDto(
            id = 1002L,
            chatId = 123L,
            senderId = 2L,
            content = "Посмотри какое фото!",
            type = "image",
            status = "read",
            createdAt = "2024-01-15T14:35:00Z",
            attachments = listOf(imageAttachment)
        )

        val imageMessage = MessageMapper.dtoToDomain(imageMessageDto)
        println("   - Type: ${imageMessage.type}")
        println("   - Attachments: ${imageMessage.attachments.size}")
        println("   - First attachment:")
        imageMessage.attachments.firstOrNull()?.let { attachment ->
            println("     * URL: ${attachment.url}")
            println("     * Type: ${attachment.type}")
            println("     * Size: ${attachment.formattedSize}")
            println("     * Dimensions: ${attachment.width}x${attachment.height}")
        }
        println("   - Preview: ${imageMessage.previewContent}")

        // 3. Тест голосового сообщения
        println("\n3. Тест голосового сообщения:")

        val audioAttachment = AttachmentDto(
            id = "attach_002",
            url = "https://example.com/audio.mp3",
            type = "audio",
            name = "voice_message.mp3",
            size = 512000,
            duration = 45
        )

        val audioMessageDto = MessageDto(
            id = 1003L,
            chatId = 123L,
            senderId = 3L,
            content = "Голосовое сообщение",
            type = "audio",
            status = "sent",
            createdAt = "2024-01-15T14:40:00Z",
            attachments = listOf(audioAttachment)
        )

        val audioMessage = MessageMapper.dtoToDomain(audioMessageDto)
        println("   - Type: ${audioMessage.type}")
        audioMessage.attachments.firstOrNull()?.let { attachment ->
            println("   - Duration: ${attachment.formattedDuration}")
            println("   - Size: ${attachment.formattedSize}")
        }

        // 4. Тест ответа на сообщение
        println("\n4. Тест ответа на сообщение:")

        val replyMessageDto = MessageDto(
            id = 1004L,
            chatId = 123L,
            senderId = 1L,
            content = "Это ответ на предыдущее сообщение",
            type = "text",
            status = "sent",
            createdAt = "2024-01-15T14:45:00Z",
            replyTo = 1001L
        )

        val replyMessage = MessageMapper.dtoToDomain(
            dto = replyMessageDto,
            replyToMessage = textMessage
        )

        println("   - Content: ${replyMessage.content}")
        println("   - Reply to: ${replyMessage.replyTo?.content}")
        println("   - Reply sender: ${replyMessage.replyTo?.senderName}")

        // 5. Тест системного сообщения
        println("\n5. Тест системного сообщения:")

        val systemMessageDto = MessageDto(
            id = 1005L,
            chatId = 123L,
            senderId = 0L,
            content = "User2 присоединился к чату",
            type = "system",
            status = "sent",
            createdAt = "2024-01-15T14:50:00Z"
        )

        val systemMessage = MessageMapper.dtoToDomain(systemMessageDto)
        println("   - Type: ${systemMessage.type}")
        println("   - Is system: ${systemMessage.isSystemMessage}")
        println("   - Preview: ${systemMessage.previewContent}")

        // 6. Тест Entity преобразования
        println("\n6. Тест Entity преобразования:")

        val messageEntity = MessageMapper.dtoToEntity(textMessageDto)
        println("   - Entity ID: ${messageEntity.id}")
        println("   - Entity chatId: ${messageEntity.chatId}")
        println("   - Entity sync status: ${messageEntity.syncStatus}")

        // 7. Тест локального сообщения
        println("\n7. Тест локального сообщения:")

        val localMessage = MessageMapper.createLocalMessage(
            chatId = "123",
            senderId = "1",
            content = "Отправляю сообщение..."
        )

        println("   - Local ID: ${localMessage.localId}")
        println("   - Status: ${localMessage.status}")
        println("   - Is sending: ${localMessage.isSending}")
        println("   - Is failed: ${localMessage.isFailed}")

        // 8. Тест обновления статуса
        println("\n8. Тест обновления статуса:")

        val deliveredMessage = MessageMapper.updateMessageStatus(localMessage, MessageStatus.DELIVERED)
        println("   - Old status: ${localMessage.status}")
        println("   - New status: ${deliveredMessage.status}")
        println("   - Is sending: ${deliveredMessage.isSending}")

        println("\n=== ТЕСТ MESSAGE ЗАВЕРШЕН ===\n")
    }
}