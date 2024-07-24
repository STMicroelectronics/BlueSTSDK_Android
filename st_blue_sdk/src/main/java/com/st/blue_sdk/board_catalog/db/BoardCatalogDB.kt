/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.st.blue_sdk.board_catalog.db.converters.BleCharacteristicDataConverter
import com.st.blue_sdk.board_catalog.db.converters.BleCharacteristicFormatDataConverter
import com.st.blue_sdk.board_catalog.db.converters.Board_compatibilityDataConverter
import com.st.blue_sdk.board_catalog.db.converters.CloudAppDataConverter
import com.st.blue_sdk.board_catalog.db.converters.CompatibleSensorAdapterDataConverter
import com.st.blue_sdk.board_catalog.db.converters.ComponentsDataConverter
import com.st.blue_sdk.board_catalog.db.converters.DemoDecoratorDataConverter
import com.st.blue_sdk.board_catalog.db.converters.FotaDetailsConverter
import com.st.blue_sdk.board_catalog.db.converters.OptionByteDataConverter
import com.st.blue_sdk.board_catalog.db.converters.PowerModeDataConverter
import com.st.blue_sdk.board_catalog.db.converters.SensorConfigurationConverter
import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.Sensor

@Database(
    version = 20,
    exportSchema = true,
    entities = [
        BoardFirmware::class,
        BoardDescription::class,
        Sensor::class,
        BleCharacteristic::class
    ]
)
@TypeConverters(
    BleCharacteristicDataConverter::class,
    OptionByteDataConverter::class,
    CloudAppDataConverter::class,
    FotaDetailsConverter::class,
    ComponentsDataConverter::class,
    CompatibleSensorAdapterDataConverter::class,
    PowerModeDataConverter::class,
    Board_compatibilityDataConverter::class,
    SensorConfigurationConverter::class,
    DemoDecoratorDataConverter::class,
    BleCharacteristicFormatDataConverter::class
)
abstract class BoardCatalogDB : RoomDatabase() {
    abstract fun boardCatalogDao(): BoardCatalogDao
}
