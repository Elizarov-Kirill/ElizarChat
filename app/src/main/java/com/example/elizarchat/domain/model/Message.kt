package com.example.elizarchat.domain.model

import java.time.Instant

data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String?,
    val type: MessageType,
    val timestamp: Instant,
    val isRead: Boolean = false,
    val chatId: String? = null
)