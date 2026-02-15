package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class JsonObjectConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @TypeConverter
    fun fromJsonObject(jsonObject: JsonObject?): String {
        return jsonObject?.toString() ?: "{}"
    }

    @TypeConverter
    fun toJsonObject(string: String): JsonObject {
        return try {
            val element = json.parseToJsonElement(string)
            element.jsonObject
        } catch (e: Exception) {
            JsonObject(emptyMap())
        }
    }
}