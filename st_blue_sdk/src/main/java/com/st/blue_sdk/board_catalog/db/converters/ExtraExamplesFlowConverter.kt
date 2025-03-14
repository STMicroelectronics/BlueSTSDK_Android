package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.ExtraExamplesFlow
import kotlinx.serialization.json.Json



class ExtraExamplesFlowConverter {
    @TypeConverter
    fun fromExtraExamplesFlow(value: List<ExtraExamplesFlow>?): String {
        return if(value!=null) {
            Json.encodeToString(value)
        } else {
            ""
        }
    }

    @TypeConverter
    fun toExtraExamplesFlow(value: String): List<ExtraExamplesFlow>? {
        if (value.isEmpty()) return null
        return Json.decodeFromString(value)
    }
}