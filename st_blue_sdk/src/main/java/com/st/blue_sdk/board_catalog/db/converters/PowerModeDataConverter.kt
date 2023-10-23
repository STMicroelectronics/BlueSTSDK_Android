package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.PowerMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PowerModeDataConverter {
    @TypeConverter
    fun fromCompatibleSensorAdapter(value: List<PowerMode>?) : String {
        return if(value!=null) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }
    @TypeConverter
    fun toCompatibleSensorAdapter(value: String) : List<PowerMode>? {
        if (value.isEmpty()) return null
        return Json.decodeFromString(value)
    }
}