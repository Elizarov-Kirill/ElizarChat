package com.example.elizarchat

import com.example.elizarchat.data.mapper.ChatMapper
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.ChatWithParticipantsDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.Chat
import com.example.elizarchat.domain.model.ChatType
import com.example.elizarchat.domain.model.User
import java.time.Instant

object TestChatComplete {
    fun runTest() {
        println("\n=== ПОЛНЫЙ ТЕСТ СУЩНОСТИ CHAT ===")

        // 1. Тест ChatDto → Domain
        println("1. Тест ChatDto → Domain:")

        val chatDto = ChatDto(
            id = 123L,
            name = "Android Developers",
            type = "group",
            avatarUrl = "https://example.com/group_avatar.jpg",
            createdBy = 999L,
            lastMessage = MessageDto(
                id = 456L,
                chatId = 123L,
                senderId = 1L,
                content = "Hello everyone!",
                type = "text",
                status = "delivered",
                createdAt = "2024-01-15T14:30:00Z"
            ),
            unreadCount = 3,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-15T14:30:00Z"
        )

        val domainChat = ChatMapper.dtoToDomain(chatDto)
        println("   - ID: ${domainChat.id}")
        println("   - Type: ${domainChat.type}")
        println("   - Display name: ${domainChat.displayName("999")}") // Используем createdBy как currentUserId
        println("   - Has unread: ${domainChat.hasUnread}")
        println("   - Unread count: ${domainChat.unreadCount}")

        // 2. Тест ChatWithParticipantsDto
        println("\n2. Тест ChatWithParticipantsDto:")

        val participants = listOf(
            UserDto(
                id = 1L,
                username = "alice",
                email = "alice@example.com",
                displayName = "Alice Smith",
                createdAt = "2024-01-01T00:00:00Z"
            ),
            UserDto(
                id = 2L,
                username = "bob",
                email = "bob@example.com",
                displayName = "Bob Johnson",
                createdAt = "2024-01-01T00:00:00Z"
            ),
            UserDto(
                id = 999L,
                username = "current",
                email = "current@example.com",
                displayName = "Current User",
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        val fullChatDto = ChatWithParticipantsDto(
            id = 123L,
            type = "group",
            name = "Android Developers",
            avatarUrl = "https://example.com/group_avatar.jpg",
            createdBy = 999L,
            participants = participants,
            lastMessage = MessageDto(
                id = 456L,
                chatId = 123L,
                senderId = 1L,
                content = "Hello everyone! This is a test message.",
                type = "text",
                status = "delivered",
                createdAt = "2024-01-15T14:30:00Z"
            ),
            unreadCount = 3,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-15T14:30:00Z"
        )

        val fullDomainChat = ChatMapper.fullDtoToDomain(fullChatDto)
        println("   - Chat ID: ${fullDomainChat.id}")
        println("   - Chat type: ${fullDomainChat.type}")
        println("   - Display name: ${fullDomainChat.displayName("999")}")
        println("   - Participants count: ${fullDomainChat.participants.size}")
        println("   - Unread messages: ${fullDomainChat.unreadCount}")
        println("   - Last message: ${fullDomainChat.lastMessage?.content}")
        println("   - Subtitle: ${fullDomainChat.subtitle("999")}")

        println("\n=== ТЕСТ CHAT ЗАВЕРШЕН ===\n")
    }
}