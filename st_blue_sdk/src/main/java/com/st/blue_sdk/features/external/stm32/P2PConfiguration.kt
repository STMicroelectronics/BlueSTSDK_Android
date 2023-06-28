/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32

import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.BoardFotaType
import com.st.blue_sdk.models.Boards

// TODO: move this item in P2P demo package
object P2PConfiguration {

    private const val PROTOCOL_V2 = 0x02
    private const val WB_ROUTER_NODE_ID = 0x85

    sealed class DeviceId(val id: Byte) {
        object Device1 : DeviceId(id = 0x01)
        object Device2 : DeviceId(id = 0x02)
        object Device3 : DeviceId(id = 0x03)
        object Device4 : DeviceId(id = 0x04)
        object Device5 : DeviceId(id = 0x05)
        object Device6 : DeviceId(id = 0x06)
        object AllDevices : DeviceId(0x00)
    }

    fun getDeviceIdById(id: Byte): DeviceId? {
        return when (id) {
            0x01.toByte() -> DeviceId.Device1
            0x02.toByte()-> DeviceId.Device2
            0x03.toByte() -> DeviceId.Device3
            0x04.toByte() -> DeviceId.Device4
            0x05.toByte() -> DeviceId.Device5
            0x06.toByte() -> DeviceId.Device6
            0x00.toByte() ->DeviceId.AllDevices
            else -> null
        }
    }

    fun getDeviceIdByBoardId(boardId: Int): DeviceId? {
        return when (boardId) {
            0x83 -> DeviceId.Device1
            0x84 -> DeviceId.Device2
            0x87 -> DeviceId.Device3
            0x88 -> DeviceId.Device4
            0x89 -> DeviceId.Device5
            0x8A -> DeviceId.Device6
            else -> null
        }
    }

    fun isValidDeviceNode(
        boardId: Int,
        protocolVersion: Short,
        boardFirmware: BoardFirmware?
    ): Boolean {

        val boardModel = Boards.getModelFromIdentifier(boardId)
        if (boardModel == Boards.Model.WB_BOARD) {
            val deviceId = runCatching { getDeviceIdByBoardId(boardId) }.getOrNull()
            return deviceId != null
        }

        if (protocolVersion.toInt() == PROTOCOL_V2) {
            boardFirmware?.let {
                return it.fota.type == BoardFotaType.WB_MODE
            }
        }

        return false
    }

    fun isValidDeviceRouter(boardId: Int): Boolean {
        val boardModel = Boards.getModelFromIdentifier(boardId)
        return boardModel == Boards.Model.WB_BOARD && boardId == WB_ROUTER_NODE_ID
    }
}