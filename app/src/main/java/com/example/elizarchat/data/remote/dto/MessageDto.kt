package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: Int,

    @SerialName("chatId")
    val chatId: Int? = null,

    @SerialName("chat_id")
    val chatIdAlt: Int? = null,

    @SerialName("userId")
    val userId: Int? = null,

    @SerialName("user_id")
    val userIdAlt: Int? = null,

    @SerialName("senderId")
    val senderId: Int? = null,

    @SerialName("sender_id")
    val senderIdAlt: Int? = null,

    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String = "text",

    @SerialName("message_type")
    val messageType: String? = null,

    // ИСПРАВЛЕНИЕ: Используем JsonElement для поддержки любого JSON
    @SerialName("metadata")
    val metadata: JsonObject = JsonObject(emptyMap()),

    @SerialName("replyTo")
    val replyTo: Int? = null,

    @SerialName("reply_to")
    val replyToAlt: Int? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("isEdited")
    val isEdited: Boolean = false,

    @SerialName("is_edited")
    val isEditedAlt: Boolean = false,

    @SerialName("isDeleted")
    val isDeleted: Boolean = false,

    @SerialName("is_deleted")
    val isDeletedAlt: Boolean = false,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("created_at")
    val createdAtAlt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("updated_at")
    val updatedAtAlt: String? = null,

    @SerialName("deletedAt")
    val deletedAt: String? = null,

    @SerialName("deleted_at")
    val deletedAtAlt: String? = null,

    @SerialName("readBy")
    val readBy: List<Int>? = emptyList(),

    @SerialName("read_by")
    val readByAlt: List<Int>? = emptyList(),

    @SerialName("sender")
    val sender: SenderDto? = null
) {
    fun getEffectiveChatId(): Int = chatId ?: chatIdAlt ?: 0
    fun getEffectiveUserId(): Int = userId ?: userIdAlt ?: senderId ?: senderIdAlt ?: 0
    fun getEffectiveCreatedAt(): String = createdAt ?: createdAtAlt ?: ""
    fun getEffectiveIsEdited(): Boolean = isEdited || isEditedAlt
    fun getEffectiveIsDeleted(): Boolean = isDeleted || isDeletedAlt
    fun getEffectiveReadBy(): List<Int> = readBy ?: readByAlt ?: emptyList()

    // Получить metadata как строку JSON
    fun getMetadataString(): String {
        return metadata.toString()
    }

    // Получить metadata как Map
    fun getMetadataMap(): Map<String, Any> {
        return try {
            when (val element = metadata) {
                is JsonObject -> {
                    element.entries.associate { (key, value) ->
                        key to when (value) {
                            is JsonPrimitive -> {
                                when {
                                    value.isString -> value.content
                                    value.booleanOrNull != null -> value.boolean
                                    value.intOrNull != null -> value.int
                                    value.longOrNull != null -> value.long
                                    value.doubleOrNull != null -> value.double
                                    else -> value.content
                                }
                            }
                            is JsonArray -> value.map { it.toString() }
                            is JsonObject -> value.toString()
                            else -> value.toString()
                        }
                    }
                }
                is JsonPrimitive -> {
                    if (element.isString) {
                        try {
                            val json = Json { ignoreUnknownKeys = true }
                            val obj = json.parseToJsonElement(element.content)
                            if (obj is JsonObject) {
                                obj.entries.associate { (key, value) ->
                                    key to value.toString()
                                }
                            } else emptyMap()
                        } catch (e: Exception) {
                            emptyMap()
                        }
                    } else emptyMap()
                }
                else -> emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

@Serializable
data class SenderDto(
    @SerialName("id")
    val id: Int,

    @SerialName("username")
    val username: String? = null,

    @SerialName("displayName")
    val displayName: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null
)

// ============ ЗАПРОСЫ ДЛЯ ОТПРАВКИ СООБЩЕНИЙ ============

@Serializable
data class SendMessageRequest(
    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String = "text",

    @SerialName("replyTo")
    val replyTo: Int? = null,

    @SerialName("metadata")
    val metadata: String = "{}"
)

@Serializable
data class UpdateMessageRequest(
    @SerialName("content")
    val content: String
)

@Serializable
data class DeleteMessageRequest(
    @SerialName("messageId")
    val messageId: Int
)

@Serializable
data class MarkAsReadRequest(
    @SerialName("messageIds")
    val messageIds: List<Int>
)