/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import com.st.blue_sdk.models.OTAMemoryAddress
import com.st.blue_sdk.models.OTAMemoryLayout
import com.st.blue_sdk.services.ota.FirmwareType
import kotlin.math.max
import kotlin.math.min

object WbOTAUtils {

    sealed class WBBoardType {

        data class Custom(val firstSectorAddress: String? = null, val sectorCount: String? = null) :
            WBBoardType()

        data object WB5xOrWB3x : WBBoardType()

        data object WB1x : WBBoardType()

        data object WBA5 : WBBoardType()

        data object WB09 : WBBoardType()

        data object WBA6 : WBBoardType()

        data object WBA2 : WBBoardType()

        data object ST67W6X : WBBoardType()
    }

    private val APPLICATION_MEMORY_LAYOUTS = listOf(
        OTAMemoryLayout(0x00.toShort(), 0x00.toShort(), 0x0000.toShort()), //Undef
        OTAMemoryLayout(0x07.toShort(), 0xD0.toShort(), 0x1000.toShort() /* 4k*/), //WB55
        OTAMemoryLayout(0x0E.toShort(), 0x44.toShort(), 0x800.toShort() /* 2k*/), //WB15
        OTAMemoryLayout(0x40.toShort(), 0x3D.toShort(), 0x2000.toShort() /* 8k*/), //WBA5
        OTAMemoryLayout(0x7F.toShort(), 0x100.toShort(), 0x800.toShort() /* 2k*/), //WB09
        OTAMemoryLayout(0x80.toShort(), 0x7A.toShort(), 0x2000.toShort() /* 8k*/), //WBA6
        OTAMemoryLayout(0x40.toShort(), 0x3A.toShort(), 0x1000.toShort() /* 4k*/), //WBA2
        OTAMemoryLayout(
            0x40.toShort(),
            0x3A.toShort(),
            0x1000.toShort() /* 4k*/
        ), //ST676X ... These values are not used
    )

    private val BLE_MEMORY_LAYOUTS = listOf(
        OTAMemoryLayout(0x000.toShort(), 0x00.toShort(), 0x0000.toShort()), //Undef
        OTAMemoryLayout(0x11.toShort(), 0xD0.toShort(), 0x1000.toShort() /* 4k*/), //WB55
        OTAMemoryLayout(0x0E.toShort(), 0x44.toShort(), 0x800.toShort() /* 2k*/), //WB15
        OTAMemoryLayout(
            0x7D.toShort(),
            0x3D.toShort(),
            0x2000.toShort() /* 8k*/
        ), //WBA5 Not Used App&BLE Stack on same binary
        OTAMemoryLayout(0xFC.toShort(), 0x100.toShort(), 0x800.toShort() /* 2k*/), //WB09
        OTAMemoryLayout(
            0xFA.toShort(),
            0x7A.toShort(),
            0x2000.toShort() /* 8k*/
        ), //WBA6 Not Used App&BLE Stack on same binary
        OTAMemoryLayout(
            0x7D.toShort(),
            0x7A.toShort(),
            0x1000.toShort() /* 4k*/
        ), //WBA2 Not Used App&BLE Stack on same binary
        OTAMemoryLayout(
            0x7D.toShort(),
            0x7A.toShort(),
            0x1000.toShort() /* 4k*/
        ), //ST676X ... These values are not used
    )

    private val MEMORY_ADDRESSES = listOf(
        OTAMemoryAddress(0x00, 0x00, 0x00), // undef
        OTAMemoryAddress(0x7000, 0x0D7000, 0x1000), // WB55
        OTAMemoryAddress(0x7000, 0x029000, 0x800), // wb15
        OTAMemoryAddress(
            0x00000,
            0x100000 - 1,
            0x2000
        ), // WBA5 // default address is 0x7C000 // previously 0xCA000 as max but problem for User Conf (default value higher than max address)
        OTAMemoryAddress(0x00000, 0x80000 - 1, 0x800), //WB09 // default address is 0x3F800
        OTAMemoryAddress(0x00000, 0x200000 - 1, 0x2000), // WBA6
        OTAMemoryAddress(0x00000, 0x80000 - 1, 0x1000), // WBA2
        OTAMemoryAddress(0x00000, 0x80000 - 1, 0x1000), // ST676X ... These values are not used
    )

    fun checkAddressAndNbSectorsInRange(
        boardType: WBBoardType,
        firmwareType: FirmwareType,
        address: String?,
        nbSectorsToErase: String?
    ): Int { //returns a code corresponding to the error
        if (boardType != WBBoardType.ST67W6X) {
            if (address.isNullOrEmpty() || address.length < 3 || address[0] != '0' || address[1] != 'x' || (address.substring(
                    2
                ).replace("[^[0-9][a-f][A-F]]".toRegex(), "").length + 2 != address.length)
            ) return 0
            if (nbSectorsToErase.isNullOrEmpty() || (nbSectorsToErase.replace(
                    "[^[0-9]]".toRegex(),
                    ""
                ).length != nbSectorsToErase.length)
            ) return 1

            val boardIndex = getSelectionIndexByBoardType(boardType)
            val addressLong = java.lang.Long.decode(address)
            val nbSectorsInt = nbSectorsToErase.toInt()

            val minAndMaxAddress = MEMORY_ADDRESSES[boardIndex]
            val maxSector = when (firmwareType) {
                FirmwareType.BOARD_FW -> APPLICATION_MEMORY_LAYOUTS[boardIndex].nSector
                FirmwareType.BLE_FW -> BLE_MEMORY_LAYOUTS[boardIndex].nSector
            }

            if (addressLong < minAndMaxAddress.min || (minAndMaxAddress.max != (-1).toLong() && addressLong > minAndMaxAddress.max)) return 2
            if (nbSectorsInt > maxSector) return 3

            if (addressLong % minAndMaxAddress.sectorSize != 0.toLong()) return 4
        }

        return -1
    }

    /*fun getFirstSectorToDelete(boardType: WBBoardType, firmwareType: FirmwareType): Short {

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
    }*/

    fun getFirstSectorToDelete(boardType: WBBoardType, address: Long): Long {
        val boardIndex = getSelectionIndexByBoardType(boardType)
        val sectorSize = MEMORY_ADDRESSES[boardIndex].sectorSize
        return address / sectorSize
    }

    fun getNumberOfSectorsToDelete(
        boardType: WBBoardType,
        firmwareType: FirmwareType,
        fileSize: Long
    ): Short {

        if (boardType != WBBoardType.ST67W6X) {
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
        } else {
            return 16 //Not used by ST676Wx
        }
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
            is WBBoardType.WBA5 -> 3
            is WBBoardType.WB09 -> 4
            is WBBoardType.WBA6 -> 5
            is WBBoardType.WBA2 -> 6
            is WBBoardType.ST67W6X -> 7
        }
    }
}