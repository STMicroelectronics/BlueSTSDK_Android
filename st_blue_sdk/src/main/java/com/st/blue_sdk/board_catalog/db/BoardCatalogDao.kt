/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware

@Dao
interface BoardCatalogDao {
    @Query("SELECT * FROM board_firmware ORDER BY ble_dev_id DESC")
    suspend fun getDeviceFirmwares(): List<BoardFirmware>
    @Query("SELECT * FROM board_description ORDER BY ble_dev_id DESC")
    suspend fun getBoardsDescription(): List<BoardDescription>?

    @Query("DELETE FROM board_firmware")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM board_description")
    suspend fun deleteAllDescEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(firmwares: List<BoardFirmware>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDesc(boardDescr: List<BoardDescription>)
}
