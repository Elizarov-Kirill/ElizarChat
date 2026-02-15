package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromJsonElement(jsonElement: JsonElement?): String {
        return jsonElement?.toString() ?: "{}"
    }

    @TypeConverter
    fun toJsonElement(string: String): JsonElement {
        return try {
            json.parseToJsonElement(string)
        } catch (e: Exception) {
            JsonObject(emptyMap())
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return json.encodeToString(list ?: emptyList())
    }

    @TypeConverter
    fun toStringList(string: String): List<String> {
        return try {
            json.decodeFromString(string)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return json.encodeToString(list ?: emptyList())
    }

    @TypeConverter
    fun toIntList(string: String): List<Int> {
        return try {
            json.decodeFromString(string)
        } catch (e: Exception) {
            emptyList()
        }
    }
}