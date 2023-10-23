package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.SensorConfiguration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SensorConfigurationConverter {
    @TypeConverter
    fun fromSensorConfiguration(value: SensorConfiguration?) : String {
        return if(value!=null) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }
    @TypeConverter
    fun toSensorConfiguration(value: String) : SensorConfiguration? {
        if (value.isEmpty()) return null
        return Json.decodeFromString(value)
    }
}