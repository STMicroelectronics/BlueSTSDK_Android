/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.db.converters

import androidx.room.TypeConverter
import com.st.blue_sdk.board_catalog.models.OptionByte
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OptionByteDataConverter {
    @TypeConverter
    fun fromOptionByte(value: List<OptionByte>): String {
        if (value.isEmpty()) return ""
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toOptionByte(value: String): List<OptionByte> {
        if (value.isEmpty()) return emptyList()
        return Json.decodeFromString(value)
    }
}
