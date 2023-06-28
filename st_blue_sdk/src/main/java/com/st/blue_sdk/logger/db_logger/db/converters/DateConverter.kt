/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db.converters

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object {
        const val DATE_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS"
    }

    @TypeConverter
    fun fromDate(value: Date): String {
        val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return formatter.format(value)
    }

    @TypeConverter
    fun toDate(value: String): Date {
        val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return formatter.parse(value)
    }
}
