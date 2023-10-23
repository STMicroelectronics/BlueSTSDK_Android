/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.OptionByte
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.advertise.getOptBytes
import com.st.blue_sdk.bt.advertise.getOptBytesOffset
import java.util.*

private const val BLE_MIN_MTU = 23 //default ble length (23-3=20 ==max packet length)
private const val BLE_PACKET_HEADER_LEN = 3

data class Node(
    val device: BluetoothDevice,
    val advertiseInfo: BleAdvertiseInfo?,
    val catalogInfo: BoardFirmware? = null,
    val fwCompatibleList: List<BoardFirmware> = emptyList(),
    val fwUpdate: BoardFirmware? = null,
    val rssi: RssiData? = null,
    val mtu: Int = BLE_MIN_MTU,
    val connectionStatus: ConnectionStatus = ConnectionStatus(),
    val deviceGatt: BluetoothGatt? = null
) {
    val isCustomFw
        get() = catalogInfo?.bleFwId == "0xFF"

    val boardType
        get() = advertiseInfo?.getBoardType() ?: Boards.Model.GENERIC

    val familyType
        get() = advertiseInfo?.getFamilyType() ?: Boards.Family.OTHER_FAMILY

    val isSleeping
        get() = advertiseInfo?.isBoardSleeping() ?: false

    val hasGeneralPurpose
        get() = advertiseInfo?.hasGeneralPurpose() ?: false

    val icons: List<Int>
        get() =
            catalogInfo?.optionBytes?.mapIndexedNotNull { i, opt ->
                val value =
                    advertiseInfo?.getOptBytes()
                        ?.get(i + 1 + advertiseInfo.getOptBytesOffset()) ?: 0

                if (value == 0xFF)
                    null
                else
                    when (OptionByte.OptionByteValueType.fromFormat(opt.format)) {
                        OptionByte.OptionByteValueType.ENUM_ICON ->

                            opt.iconValues.find { it.value == value }?.iconCode

                        OptionByte.OptionByteValueType.INT, OptionByte.OptionByteValueType.ENUM_STRING,
                        OptionByte.OptionByteValueType.UNKNOWN -> null
                    }
            } ?: emptyList()

    val displayMessages: List<String>
        get() =
            catalogInfo?.optionBytes?.mapIndexedNotNull { i, opt ->
                val value =
                    advertiseInfo?.getOptBytes()
                        ?.get(i + 1 + advertiseInfo.getOptBytesOffset()) ?: 0

                when (OptionByte.OptionByteValueType.fromFormat(opt.format)) {
                    OptionByte.OptionByteValueType.INT -> {
                        if (opt.escapeValue == value) {
                            opt.escapeMessage
                        } else {
                            val negativeOffset = opt.negativeOffset ?: 0
                            val scaleFactor = opt.scaleFactor ?: 0
                            String.format(
                                Locale.getDefault(),
                                "%s %d%s",
                                opt.name,
                                (value - negativeOffset) * scaleFactor,
                                opt.type ?: ""
                            )
                        }
                    }
                    OptionByte.OptionByteValueType.ENUM_STRING -> {
                        String.format(
                            Locale.getDefault(),
                            "%s %s",
                            opt.name,
                            opt.stringValues?.find { it.value == value }?.displayName ?: ""
                        )
                    }
                    OptionByte.OptionByteValueType.ENUM_ICON,
                    OptionByte.OptionByteValueType.UNKNOWN -> null
                }
            } ?: emptyList()

    val runningFw = catalogInfo?.let {
        String.format(
            "%s Running Fw:\n%s v%s",
            catalogInfo.brdName,
            catalogInfo.fwName,
            catalogInfo.fwVersion
        )
    }

    //FixLP
    var maxPayloadSize = mtu - BLE_PACKET_HEADER_LEN

    val friendlyName: String
        get() {
            val tag = if (device.address.isNullOrEmpty()) {
                "NA"
            } else {
                device.address.replace(":", "").takeLast(6)
            }
            return "${advertiseInfo?.getName()} @${tag}"
        }
}

data class ConnectionStatus(
    val prev: NodeState = NodeState.Disconnected,
    val current: NodeState = NodeState.Disconnected
)

data class RssiData(val rssi: Int, val timestamp: Date)