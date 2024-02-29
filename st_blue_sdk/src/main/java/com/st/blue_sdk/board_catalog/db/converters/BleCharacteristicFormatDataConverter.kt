package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.BleCharacteristicFormat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BleCharacteristicFormatDataConverter {
    @TypeConverter
    fun fromBleCharacteristicFormat(value: List<BleCharacteristicFormat>): String {
        if (value.isEmpty()) return ""
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toBleCharacteristicFormat(value: String): List<BleCharacteristicFormat> {
        if (value.isEmpty()) return emptyList()
        return Json.decodeFromString(value)
    }
}