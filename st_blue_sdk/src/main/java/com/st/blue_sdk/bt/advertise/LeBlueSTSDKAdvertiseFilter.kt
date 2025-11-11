/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import android.util.SparseArray
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.utils.NumberConversion
//import kotlin.String

class LeBlueSTSDKAdvertiseFilter : LeAdvertiseFilter {

    companion object {
        private const val VERSION_PROTOCOL_SUPPORTED_MIN: Byte = 0x03
        private const val VERSION_PROTOCOL_SUPPORTED_MAX: Byte = 0x03
        private const val DEVICE_NAME_TYPE: Byte = 0x09
        private const val VENDOR_DATA_TYPE: Byte = 0xFF.toByte()
    }

    override fun decodeAdvertiseData(nodeId: String,advertisingData: ByteArray): LeBleAdvertiseInfo? {

        val splitAdv: SparseArray<ByteArray> = AdvertiseParser.split(advertisingData)

        var data = splitAdv[DEVICE_NAME_TYPE.toInt()]
        val name = data?.let { String(it) } ?: ""

        data = splitAdv[VENDOR_DATA_TYPE.toInt()]
        data?.let {

            var offset = 0

            //Check on min length:
            //  - 2 for ST's Manufacture ID
            //  - 1 for BlueST-SDK Version
            //  - 1: for DeviceId
            //  - 1: for FirmwareId
            //  - 2: for ProtocolId
            //  - 1: at least one byte used for payload
            if(it.size <(2+1+1+1+2+1)) {
                return null
            }

            //Check if ww
            if (it[0].toInt() != 0x30 && it[1].toInt() != 0x00) {
                return null
            } else {
                offset = 2
            }

            val protocolVersion: Short = NumberConversion.byteToUInt8(it, offset)
            if (protocolVersion < VERSION_PROTOCOL_SUPPORTED_MIN || protocolVersion > VERSION_PROTOCOL_SUPPORTED_MAX) {
                return null
            }

            val deviceId = it[1 + offset].toInt()

            //we use the same boardId used for BlueST-SDK V2
            val model: Boards.Model = Boards.getModelFromIdentifier(deviceId, 0x02)

            val firmwareId = it[2 + offset].toInt()

            val protocolId =  NumberConversion.BigEndian.bytesToUInt16(it, 3+offset)

            val payLoad = it.copyOfRange(5+offset, it.size)

//            val address = if (it.size != 6) {
//                //val address = if (it.size != (6+2)) {
//                String.format(
//                    "%02X:%02X:%02X:%02X:%02X:%02X",
//                    it[6 + offset], it[7 + offset], it[8 + offset],
//                    it[9 + offset], it[10 + offset], it[11 + offset]
//                )
//            } else null

            return LeBlueStSdkAdvertiseInfo(
                name = name,
                address = nodeId,
                deviceId = deviceId,
                firmwareId = firmwareId,
                protocolId = protocolId,
                payloadData = payLoad,
                protocolVersion = protocolVersion,
                model = model
            )
        }

        return null
    }
}