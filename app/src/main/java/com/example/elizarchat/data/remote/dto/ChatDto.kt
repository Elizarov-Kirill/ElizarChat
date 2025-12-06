package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    @SerialName("id")
    val id: Long,

    @SerialName("type")
    val type: String, // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null,

    @SerialName("createdBy")
    val createdBy: Long? = null,

    @SerialName("lastMessage")
    val lastMessage: MessageDto? = null,

    @SerialName("unreadCount")
    val unreadCount: Int = 0,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null
)

@Serializable
data class ChatWithParticipantsDto(
    @SerialName("id")
    val id: Long,

    @SerialName("type")
    val type: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null,

    @SerialName("createdBy")
    val createdBy: Long? = null,

    @SerialName("participants")
    val participants: List<UserDto> = emptyList(),

    @SerialName("lastMessage")
    val lastMessage: MessageDto? = null,

    @SerialName("unreadCount")
    val unreadCount: Int = 0,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null
)

@Serializable
data class CreateChatRequestDto(
    @SerialName("type")
    val type: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("participantIds")
    val participantIds: List<Long>
)

@Serializable
data class ChatsResponseDto(
    @SerialName("chats")
    val chats: List<ChatDto> = emptyList()
)