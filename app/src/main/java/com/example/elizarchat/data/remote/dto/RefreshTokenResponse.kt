package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("accessToken")
    val accessToken: String? = null,

    @SerialName("tokens")
    val tokens: Tokens? = null,

    @SerialName("error")
    val error: String? = null
)

@Serializable
data class Tokens(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String? = null
)