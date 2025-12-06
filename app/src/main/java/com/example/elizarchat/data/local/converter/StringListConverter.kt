package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return if (list == null) null else gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        return if (json == null) emptyList() else
            gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
}