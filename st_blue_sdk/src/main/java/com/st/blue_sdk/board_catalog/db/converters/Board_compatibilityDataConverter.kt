package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Board_compatibilityDataConverter {
    @TypeConverter
    fun fromBoard_compatibility(value: ArrayList<String>) : String {
        return if(value.isNotEmpty()) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }
    @TypeConverter
    fun toBoard_compatibility(value: String) : ArrayList<String> {
        if (value.isEmpty()) return ArrayList()
        return Json.decodeFromString(value)
    }
}