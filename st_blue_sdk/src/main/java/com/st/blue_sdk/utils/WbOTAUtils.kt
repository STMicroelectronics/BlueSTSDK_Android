/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.util.Log
import com.st.blue_sdk.models.OTAMemoryAddress
import com.st.blue_sdk.models.OTAMemoryLayout
import com.st.blue_sdk.services.ota.FirmwareType
import kotlin.math.max
import kotlin.math.min

object WbOTAUtils {

    sealed class WBBoardType {

        data class Custom(val firstSectorAddress: String? = null, val sectorCount: String? = null) :
            WBBoardType()

        object WB5xOrWB3x : WBBoardType()

        object WB1x : WBBoardType()

        object WBA : WBBoardType()
    }

    private val APPLICATION_MEMORY_LAYOUTS = listOf(
        OTAMemoryLayout(0x00.toShort(), 0x00.toShort(), 0x0000.toShort()), //Undef
        OTAMemoryLayout(0x07.toShort(), 0x7F.toShort(), 0x1000.toShort() /* 4k*/), //WB55
        OTAMemoryLayout(0x0E.toShort(), 0x24.toShort(), 0x800.toShort() /* 2k*/), //WB15
        OTAMemoryLayout(0x3E.toShort(), 0x3F.toShort(), 0x2000.toShort() /* 8k*/) //WBA
    )

    private val BLE_MEMORY_LAYOUTS = listOf(
        OTAMemoryLayout(0x000.toShort(), 0x00.toShort(), 0x0000.toShort()), //Undef
        OTAMemoryLayout(0x0F.toShort(), 0x7F.toShort(), 0x1000.toShort() /* 4k*/), //WB55
        OTAMemoryLayout(0x0E.toShort(), 0x3C.toShort(), 0x800.toShort() /* 2k*/), //WB15
        OTAMemoryLayout(0x7B.toShort(), 0x3F.toShort(), 0x2000.toShort() /* 8k*/) //WBA
    )

    private val MEMORY_ADDRESSES = listOf(
        OTAMemoryAddress(0x00, 0x00, 0x00), // undef
        OTAMemoryAddress(0x7000, 0x089000, 0x1000), // wb
        OTAMemoryAddress(0x7000, 0x01C000, 0x800), // wb15
        OTAMemoryAddress(0x7C000, 0xCA000, 0x2000), // wba //true min is 0x28000 but as the min is the one actually taken, we put the default value as the min

    )

    fun getFirstSectorToDelete(boardType: WBBoardType, firmwareType: FirmwareType): Short {

        val boardIndex = getSelectionIndexByBoardType(boardType)

        if (boardType is WBBoardType.Custom) {
            return try {
                boardType.firstSectorAddress!!.toShort(10)
            } catch (e: NumberFormatException) {
                APPLICATION_MEMORY_LAYOUTS[boardIndex].fistSector
            }
        }

        return when (firmwareType) {
            FirmwareType.BOARD_FW -> APPLICATION_MEMORY_LAYOUTS[boardIndex].fistSector
            FirmwareType.BLE_FW -> BLE_MEMORY_LAYOUTS[boardIndex].fistSector
        }
    }

    fun getNumberOfSectorsToDelete(
        boardType: WBBoardType,
        firmwareType: FirmwareType,
        fileSize: Long
    ): Short {

        val boardIndex = getSelectionIndexByBoardType(boardType)

        //File Size in Sectors
        //IMPORTANT the code works only if the sector sze of APPLICATION_MEMORY== BLE_MEMORY
        val sectorsToDeleteByFileDimension =
            ((fileSize + APPLICATION_MEMORY_LAYOUTS[boardIndex].sectorSize - 1) / APPLICATION_MEMORY_LAYOUTS[boardIndex].sectorSize)

        val out =
            if (boardType is WBBoardType.Custom) {
                try {
                    boardType.sectorCount!!.toShort(10)
                } catch (e: NumberFormatException) {
                    APPLICATION_MEMORY_LAYOUTS[boardIndex].nSector
                }
            } else {
                when (firmwareType) {
                    FirmwareType.BOARD_FW -> APPLICATION_MEMORY_LAYOUTS[boardIndex].nSector
                    FirmwareType.BLE_FW -> BLE_MEMORY_LAYOUTS[boardIndex].nSector
                }
            }

        return if (sectorsToDeleteByFileDimension < out) sectorsToDeleteByFileDimension.toShort() else out
    }

    fun getMemoryAddress(boardType: WBBoardType, selectedAddress: Long? = null): Long {

        val boardIndex = getSelectionIndexByBoardType(boardType)

        val boardAddressSpec = MEMORY_ADDRESSES[boardIndex]

        val address = selectedAddress ?: boardAddressSpec.min

        if (address % boardAddressSpec.sectorSize != 0.toLong()) {
            throw IllegalArgumentException("Memory address should be multiple of Board Sector Size")
        }

        if (address !in boardAddressSpec.min..boardAddressSpec.max) {
            throw IllegalArgumentException("Invalid memory address")
        }

        //clamp
        return max(boardAddressSpec.min, min(address, boardAddressSpec.max))
    }

    private fun getSelectionIndexByBoardType(boardType: WBBoardType): Int {
        return when (boardType) {
            is WBBoardType.Custom -> 0
            is WBBoardType.WB5xOrWB3x -> 1
            is WBBoardType.WB1x -> 2
            is WBBoardType.WBA -> 3
        }
    }
}