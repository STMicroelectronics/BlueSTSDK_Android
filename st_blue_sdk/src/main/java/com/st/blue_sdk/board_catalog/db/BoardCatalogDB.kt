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
import com.st.blue_sdk.board_catalog.db.converters.CloudAppDataConverter
import com.st.blue_sdk.board_catalog.db.converters.ComponentsDataConverter
import com.st.blue_sdk.board_catalog.db.converters.FotaDetailsConverter
import com.st.blue_sdk.board_catalog.db.converters.OptionByteDataConverter
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware

@Database(
    version = 6,
    exportSchema = true,
    entities = [
        BoardFirmware::class,
        BoardDescription::class
    ]
)
@TypeConverters(
    BleCharacteristicDataConverter::class,
    OptionByteDataConverter::class,
    CloudAppDataConverter::class,
    FotaDetailsConverter::class,
    ComponentsDataConverter::class
)
abstract class BoardCatalogDB : RoomDatabase() {
    abstract fun boardCatalogDao(): BoardCatalogDao
}
