package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.ChatEntity
import com.example.elizarchat.data.local.entity.ChatMemberEntity
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.ChatMemberDto
import com.example.elizarchat.domain.model.Chat
import com.example.elizarchat.domain.model.ChatMember
import com.example.elizarchat.domain.model.ChatType
import com.example.elizarchat.domain.model.MemberRole
import com.example.elizarchat.domain.model.Message
import com.example.elizarchat.domain.model.MessageType
import com.example.elizarchat.domain.model.User
import java.time.Instant
import java.time.format.DateTimeParseException

object ChatMapper {

    // === ЧАТ: DTO → Entity ===
    fun chatDtoToEntity(dto: ChatDto): ChatEntity {
        return ChatEntity(
            id = dto.id,
            type = dto.type,
            name = dto.name,
            description = dto.description,
            avatarUrl = null, // нет в серверном DTO
            createdBy = dto.createdBy,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            lastMessageId = dto.lastMessageId,
            // Локальные поля по умолчанию
            unreadCount = 0,
            isMuted = false,
            isPinned = false,
            lastSyncAt = Instant.now(),
            syncStatus = "SYNCED"
        )
    }

    // === ЧАТ: Entity → Domain ===
    fun chatEntityToDomain(
        entity: ChatEntity,
        members: List<ChatMember> = emptyList(),
        lastMessageText: String? = null
    ): Chat {
        return Chat(
            id = entity.id,
            type = parseChatType(entity.type),
            name = entity.name,
            description = entity.description,
            avatarUrl = entity.avatarUrl,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            lastMessageId = entity.lastMessageId,
            // Локальные поля
            unreadCount = entity.unreadCount,
            isMuted = entity.isMuted,
            isPinned = entity.isPinned,
            participants = members,
            lastMessage = lastMessageText?.let {
                // Создаем заглушку для последнего сообщения
                Message(
                    id = entity.lastMessageId ?: "",
                    chatId = entity.id,
                    userId = "",
                    content = it,
                    messageType = MessageType.TEXT,
                    createdAt = entity.updatedAt ?: entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        )
    }

    // === ЧАТ: DTO → Domain ===
    fun chatDtoToDomain(
        dto: ChatDto,
        members: List<ChatMember> = emptyList(),
        lastMessageText: String? = null
    ): Chat {
        return Chat(
            id = dto.id,
            type = parseChatType(dto.type),
            name = dto.name,
            description = dto.description,
            avatarUrl = null,
            createdBy = dto.createdBy,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            lastMessageId = dto.lastMessageId,
            // Локальные поля по умолчанию
            unreadCount = 0,
            isMuted = false,
            isPinned = false,
            participants = members,
            lastMessage = lastMessageText?.let {
                Message(
                    id = dto.lastMessageId ?: "",
                    chatId = dto.id,
                    userId = "",
                    content = it,
                    messageType = MessageType.TEXT,
                    createdAt = parseInstant(dto.updatedAt) ?: Instant.now(),
                    updatedAt = parseInstant(dto.updatedAt)
                )
            }
        )
    }

    // === ЧАТ: Domain → Entity ===
    fun chatDomainToEntity(domain: Chat): ChatEntity {
        return ChatEntity(
            id = domain.id,
            type = domain.type.name.lowercase(),
            name = domain.name,
            description = domain.description,
            avatarUrl = domain.avatarUrl,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            lastMessageId = domain.lastMessageId,
            // Локальные поля
            unreadCount = domain.unreadCount,
            isMuted = domain.isMuted,
            isPinned = domain.isPinned,
            lastSyncAt = Instant.now(),
            syncStatus = if (domain.isMuted || domain.isPinned || domain.unreadCount > 0) {
                "DIRTY"
            } else {
                "SYNCED"
            }
        )
    }

    // === УЧАСТНИК: DTO → Entity ===
    fun memberDtoToEntity(dto: ChatMemberDto): ChatMemberEntity {
        return ChatMemberEntity(
            id = dto.id,
            chatId = dto.chatId,
            userId = dto.userId,
            role = dto.role,
            joinedAt = parseInstant(dto.joinedAt) ?: Instant.now(),
            lastReadMessageId = dto.lastReadMessageId,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt)
        )
    }

    // === УЧАСТНИК: Entity → Domain ===
    fun memberEntityToDomain(
        entity: ChatMemberEntity,
        user: User
    ): ChatMember {
        return ChatMember(
            id = entity.id,
            chatId = entity.chatId,
            user = user,
            role = parseMemberRole(entity.role),
            joinedAt = entity.joinedAt,
            lastReadMessageId = entity.lastReadMessageId
        )
    }

    // === УЧАСТНИК: DTO → Domain ===
    fun memberDtoToDomain(
        dto: ChatMemberDto,
        user: User
    ): ChatMember {
        return ChatMember(
            id = dto.id,
            chatId = dto.chatId,
            user = user,
            role = parseMemberRole(dto.role),
            joinedAt = parseInstant(dto.joinedAt) ?: Instant.now(),
            lastReadMessageId = dto.lastReadMessageId
        )
    }

    // === УЧАСТНИК: Domain → Entity ===
    fun memberDomainToEntity(domain: ChatMember): ChatMemberEntity {
        return ChatMemberEntity(
            id = domain.id,
            chatId = domain.chatId,
            userId = domain.user.id,
            role = domain.role.name.lowercase(),
            joinedAt = domain.joinedAt,
            lastReadMessageId = domain.lastReadMessageId,
            createdAt = Instant.now(),
            updatedAt = null
        )
    }

    // === Вспомогательные методы ===

    private fun parseChatType(typeString: String): ChatType {
        return when (typeString.lowercase()) {
            "private" -> ChatType.PRIVATE
            "group" -> ChatType.GROUP
            "channel" -> ChatType.CHANNEL
            else -> ChatType.GROUP
        }
    }

    private fun parseMemberRole(roleString: String): MemberRole {
        return when (roleString.lowercase()) {
            "owner" -> MemberRole.OWNER
            "admin" -> MemberRole.ADMIN
            "member" -> MemberRole.MEMBER
            "guest" -> MemberRole.GUEST
            else -> MemberRole.MEMBER
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

    fun chatDtosToEntities(dtos: List<ChatDto>): List<ChatEntity> {
        return dtos.map { chatDtoToEntity(it) }
    }

    fun chatEntitiesToDomains(
        entities: List<ChatEntity>,
        membersMap: Map<String, List<ChatMember>> = emptyMap(),
        lastMessagesMap: Map<String, String> = emptyMap()
    ): List<Chat> {
        return entities.map { entity ->
            chatEntityToDomain(
                entity = entity,
                members = membersMap[entity.id] ?: emptyList(),
                lastMessageText = lastMessagesMap[entity.id]
            )
        }
    }

    fun chatDtosToDomains(
        dtos: List<ChatDto>,
        membersMap: Map<String, List<ChatMember>> = emptyMap(),
        lastMessagesMap: Map<String, String> = emptyMap()
    ): List<Chat> {
        return dtos.map { dto ->
            chatDtoToDomain(
                dto = dto,
                members = membersMap[dto.id] ?: emptyList(),
                lastMessageText = lastMessagesMap[dto.id]
            )
        }
    }

    fun memberDtosToEntities(dtos: List<ChatMemberDto>): List<ChatMemberEntity> {
        return dtos.map { memberDtoToEntity(it) }
    }

    fun memberEntitiesToDomains(
        entities: List<ChatMemberEntity>,
        usersMap: Map<String, User>
    ): List<ChatMember> {
        return entities.mapNotNull { entity ->
            usersMap[entity.userId]?.let { user ->
                memberEntityToDomain(entity, user)
            }
        }
    }

    // === Обновление сущностей ===

    fun updateChatEntity(existing: ChatEntity, dto: ChatDto): ChatEntity {
        return existing.copy(
            name = dto.name ?: existing.name,
            description = dto.description ?: existing.description,
            updatedAt = parseInstant(dto.updatedAt) ?: existing.updatedAt,
            lastMessageId = dto.lastMessageId ?: existing.lastMessageId,
            lastSyncAt = Instant.now(),
            syncStatus = if (existing.syncStatus == "DIRTY") {
                "DIRTY"
            } else {
                "SYNCED"
            }
        )
    }

    fun updateMemberEntity(existing: ChatMemberEntity, dto: ChatMemberDto): ChatMemberEntity {
        return existing.copy(
            role = dto.role,
            lastReadMessageId = dto.lastReadMessageId ?: existing.lastReadMessageId,
            updatedAt = Instant.now()
        )
    }
}