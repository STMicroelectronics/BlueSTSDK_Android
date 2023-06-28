/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import android.util.SparseArray
import java.util.*

class BlueNRGAdvertiseFilter : AdvertiseFilter {

    companion object {

        private const val DEFAULT_NAME = "BlueNRG OTA"

        private const val DEVICE_NAME_TYPE: Byte = 0x09
        private const val INCOMPLETE_LIST_OF_128_UUID: Byte = 0x06

        private val OTA_SERVICE_UUID = byteArrayOf(
            0x8a.toByte(),
            0x97.toByte(),
            0xf7.toByte(),
            0xc0.toByte(),
            0x85.toByte(),
            0x06.toByte(),
            0x11.toByte(),
            0xe3.toByte(),
            0xba.toByte(),
            0xa7.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x20.toByte(),
            0x0c.toByte(),
            0x9a.toByte(),
            0x66.toByte()
        )
    }

    private fun getDeviceName(advData: SparseArray<ByteArray>): String {
        val nameData = advData[DEVICE_NAME_TYPE.toInt()]
        return if (nameData != null && nameData.isNotEmpty()) String(nameData) else DEFAULT_NAME
    }

    override fun decodeAdvertiseData(advertisingData: ByteArray): BleAdvertiseInfo? {

        val splitAdv: SparseArray<ByteArray> = AdvertiseParser.split(advertisingData)
        val exportedService = splitAdv[INCOMPLETE_LIST_OF_128_UUID.toInt()]

        return if (exportedService != null && Arrays.equals(OTA_SERVICE_UUID, exportedService)) {
            BlueNRGAdvertiseInfo(getDeviceName(splitAdv))
        } else null
    }
}