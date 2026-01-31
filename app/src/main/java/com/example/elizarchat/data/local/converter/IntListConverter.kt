package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Конвертер для преобразования List<Int> ↔ JSON строка
 * Используется для поля read_by в сообщениях
 */
class IntListConverter {

    @TypeConverter
    fun fromJson(json: String?): List<Int> {
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString<List<Int>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun toJson(list: List<Int>): String {
        return try {
            Json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }
}