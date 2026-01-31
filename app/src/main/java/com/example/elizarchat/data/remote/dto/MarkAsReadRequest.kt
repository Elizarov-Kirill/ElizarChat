package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkAsReadRequest(
    @SerialName("message_id")
    val messageId: Int
)