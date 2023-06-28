/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.st.blue_sdk.logger.db_logger.db.converters.DateConverter
import com.st.blue_sdk.logger.db_logger.db.model.FeatureLog
import com.st.blue_sdk.logger.db_logger.db.model.FeatureUpdateLog

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        FeatureLog::class,
        FeatureUpdateLog::class
    ]
)
@TypeConverters(DateConverter::class)
abstract class LoggerDB : RoomDatabase() {
    abstract fun loggerDao(): LoggerDao
}
