package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class ServerInfoResponse(
    @SerialName("version")
    val version: String,

    @SerialName("environment")
    val environment: String,

    @SerialName("uptime")
    val uptime: String? = null,

    @SerialName("database")
    val database: DatabaseStatus? = null
)

@Serializable
data class DatabaseStatus(
    @SerialName("connected")
    val connected: Boolean,

    @SerialName("latency")
    val latency: Long? = null
)