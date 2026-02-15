package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ USER DTOs ============
@Serializable
data class UserDto(
    @SerialName("id")
    val id: Int,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String? = null,

    @SerialName("display_name")  // ← ИСПРАВЛЕНО: snake_case!
    val displayName: String? = null,

    @SerialName("avatar_url")    // ← ИСПРАВЛЕНО: snake_case!
    val avatarUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("is_online")
    val isOnline: Boolean = false,

    @SerialName("last_seen")
    val lastSeen: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("settings")
    val settings: String? = null
)



@Serializable
data class UpdateSettingsRequest(
    @SerialName("settings")
    val settings: String  // JSON строка
)


// ============ SEARCH ============
@Serializable
data class SearchUsersRequest(
    @SerialName("query")
    val query: String,

    @SerialName("page")
    val page: Int = 1,

    @SerialName("limit")
    val limit: Int = 20
)