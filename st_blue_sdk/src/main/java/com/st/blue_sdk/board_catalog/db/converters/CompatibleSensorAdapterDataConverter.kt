package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CompatibleSensorAdapterDataConverter {
    @TypeConverter
    fun fromCompatibleSensorAdapter(value: List<Int>?) : String {
        return if(value!=null) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }
    @TypeConverter
    fun toCompatibleSensorAdapter(value: String) : List<Int>? {
        if (value.isEmpty()) return null
        return Json.decodeFromString(value)
    }
}