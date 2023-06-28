/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import com.st.blue_sdk.utils.NumberConversion

data class AdvertiseFwInfo(val deviceId: String, val fwId: String)

fun BleAdvertiseInfo.getFwInfo(): AdvertiseFwInfo? {

    val optBytes = NumberConversion.BigEndian.uint32ToBytes(getOptionBytes())
    val optByte0 = NumberConversion.byteToUInt8(optBytes, 0).toInt()
    val optByte1 = NumberConversion.byteToUInt8(optBytes, 1).toInt()

    val bleFwId =
        if (optByte0 != 0x00) {
            optByte0
        } else {
            optByte1 + 256
        }

    if (bleFwId < 0) {
        return null
    }

    return AdvertiseFwInfo("0x%02X".format(getDeviceId()), "0x%02X".format(bleFwId))
}

fun BleAdvertiseInfo.getOptBytes(): List<Int> {
    val optBytes = NumberConversion.BigEndian.uint32ToBytes(getOptionBytes())
    return listOf(
        NumberConversion.byteToUInt8(optBytes, 0).toInt(),
        NumberConversion.byteToUInt8(optBytes, 1).toInt(),
        NumberConversion.byteToUInt8(optBytes, 2).toInt(),
        NumberConversion.byteToUInt8(optBytes, 3).toInt()
    )
}

fun BleAdvertiseInfo.getOptBytesOffset(): Int {
    val optBytes = NumberConversion.BigEndian.uint32ToBytes(getOptionBytes())
    val optByte0 = NumberConversion.byteToUInt8(optBytes, 0).toInt()
    return if (optByte0 == 0x00) 1 else 0
}