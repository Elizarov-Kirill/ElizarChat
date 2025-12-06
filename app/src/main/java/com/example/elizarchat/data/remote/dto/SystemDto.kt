package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status")
    val status: String,

    @SerialName("timestamp")
    val timestamp: String,

    @SerialName("service")
    val service: String,

    @SerialName("version")
    val version: String,

    @SerialName("services")
    val services: ServicesStatus? = null
)

@Serializable
data class ServicesStatus(
    @SerialName("database")
    val database: Boolean = false,

    @SerialName("redis")
    val redis: Boolean = false,

    @SerialName("storage")
    val storage: Boolean = false
)

@Serializable
data class ServerInfoResponse(
    @SerialName("name")
    val name: String,

    @SerialName("version")
    val version: String,

    @SerialName("environment")
    val environment: String,

    @SerialName("uptime")
    val uptime: String? = null
)