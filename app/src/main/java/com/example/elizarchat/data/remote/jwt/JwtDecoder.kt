package com.example.elizarchat.data.remote.jwt

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.time.Instant

// ВРЕМЕННОЕ РЕШЕНИЕ: упрощенный декодер
object JwtDecoder {

    fun decodePayload(jwt: String): JwtPayload? {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return null

            val payloadJson = String(
                Base64.decode(parts[1], Base64.URL_SAFE),
                StandardCharsets.UTF_8
            )

            val json = JSONObject(payloadJson)

            JwtPayload(
                userId = json.optInt("userId", -1),
                username = json.optString("username"),
                exp = json.optLong("exp", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenExpired(jwt: String): Boolean {
        val payload = decodePayload(jwt) ?: return true
        if (payload.exp == 0L) return true

        val expiryTime = Instant.ofEpochSecond(payload.exp)
        return Instant.now().isAfter(expiryTime)
    }

    fun getExpiryTimeMillis(jwt: String): Long {
        val payload = decodePayload(jwt) ?: return 0L
        if (payload.exp == 0L) return 0L
        return payload.exp * 1000L
    }

    fun getUserId(jwt: String): Int {
        return decodePayload(jwt)?.userId ?: -1
    }
}

data class JwtPayload(
    val userId: Int,
    val username: String,
    val exp: Long
)