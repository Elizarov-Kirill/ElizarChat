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
    val email: String? = null,  // ⚠️ ОБРАТИТЕ ВНИМАНИЕ: В ответе регистрации email отсутствует!

    @SerialName("displayName")  // ✅ ИСПРАВЛЕНО: camelCase
    val displayName: String? = null,

    @SerialName("avatarUrl")  // ✅ ИСПРАВЛЕНО: camelCase
    val avatarUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("isOnline")  // ✅ ИСПРАВЛЕНО: camelCase
    val isOnline: Boolean = false,

    @SerialName("lastSeen")  // ✅ ИСПРАВЛЕНО: camelCase
    val lastSeen: String? = null,

    @SerialName("createdAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val createdAt: String? = null,

    @SerialName("updatedAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val updatedAt: String? = null,

    @SerialName("settings")
    val settings: String? = null  // JSON строка
)

// ============ USER OPERATIONS ============
@Serializable
data class UpdateProfileRequest(
    @SerialName("displayName")  // ✅ ИСПРАВЛЕНО: camelCase
    val displayName: String? = null,

    @SerialName("avatarUrl")  // ✅ ИСПРАВЛЕНО: camelCase
    val avatarUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("status")
    val status: String? = null
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("currentPassword")  // ✅ ИСПРАВЛЕНО: camelCase
    val currentPassword: String,

    @SerialName("newPassword")  // ✅ ИСПРАВЛЕНО: camelCase
    val newPassword: String
)

@Serializable
data class UpdateSettingsRequest(
    @SerialName("settings")
    val settings: String  // JSON строка
)

// ============ LOCAL SETTINGS ============
@Serializable
data class UserSettings(
    @SerialName("notifications")
    val notifications: Boolean = true,

    @SerialName("theme")
    val theme: String = "light",

    @SerialName("language")
    val language: String = "en"
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