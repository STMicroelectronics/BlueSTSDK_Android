package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.DemoDecorator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DemoDecoratorDataConverter {
    @TypeConverter
    fun fromDemoDecorator(value: DemoDecorator): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toDemoDecorator(value: String): DemoDecorator {
        return Json.decodeFromString(value)
    }
}