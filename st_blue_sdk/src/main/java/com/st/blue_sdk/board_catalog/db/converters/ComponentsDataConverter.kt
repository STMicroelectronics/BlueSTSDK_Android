package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ComponentsDataConverter {
    @TypeConverter
    fun fromComponents(value: List<String>?) : String {
        return if(value!=null) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }
    @TypeConverter
    fun toComponents(value: String) : List<String>? {
        if (value.isEmpty()) return null
        return Json.decodeFromString(value)
    }
}