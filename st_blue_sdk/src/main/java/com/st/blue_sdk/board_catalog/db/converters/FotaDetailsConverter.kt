package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.FotaDetails
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FotaDetailsConverter {
    @TypeConverter
    fun fromFotaDetails(value: FotaDetails): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toFotaDetails(value: String): FotaDetails {
        return Json.decodeFromString(value)
    }
}