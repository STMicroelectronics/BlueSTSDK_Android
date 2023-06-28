/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.std

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class HeartRate(
    isEnabled: Boolean,
    type: Type,
    identifier: Int,
    name: String = NAME
) : Feature<HeartRateInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = false
) {

    companion object {
        const val NAME = "Heart Rate"

        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<HeartRateInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val heartRate: Int
        val energyExpended: Int
        val rrInterval: Float
        var offset = dataOffset

        val flags = NumberConversion.byteToUInt8(data,offset).toInt()
        offset++
        if (has8BitHeartRate(flags)
        ) {
            heartRate = NumberConversion.byteToUInt8(data,offset).toInt()
            offset++
        } else {
            heartRate = NumberConversion.LittleEndian.bytesToUInt16(data, offset)
            offset += 2
        }

        val skinContactDetected: Boolean = flags and 0x02 != 0   // Bit 1 of flags
        val skinContactSupported: Boolean = flags and 0x04 != 0   // Bit 2 of flags

        if (hasEnergyExpended(flags)
        ) {
            energyExpended = NumberConversion.LittleEndian.bytesToUInt16(data, offset)
            offset += 2
        } else {
            energyExpended = -1
        }

        if (hasRRInterval(flags)) {
            rrInterval = NumberConversion.LittleEndian.bytesToUInt16(data, offset) / 1024.0f
            offset += 2
        } else {
            rrInterval = Float.NaN
        }

        return FeatureUpdate(
            readByte = offset-dataOffset,
            rawData = data,
            timeStamp = timeStamp,
            data = HeartRateInfo(
                heartRate = FeatureField(
                    name = "Heart Rate Measurement",
                    unit = "bpm",
                    min = 0,
                    max = UShort.MAX_VALUE.toInt(),
                    value = heartRate
                ),
                energyExpended = FeatureField(
                    name = "Energy Expended",
                    unit = "kJ",
                    min = -1,
                    max = UShort.MAX_VALUE.toInt(),
                    value = energyExpended
                ),
                rrInterval = FeatureField(
                    name = "RR-Interval",
                    unit = "s",
                    max = 0f,
                    min = Float.MAX_VALUE,
                    value = rrInterval
                ),
                skinContactSupported = FeatureField(
                    name ="Skin Contact Supported",
                    value = skinContactSupported
                ),
                skinContactDetected = FeatureField(
                    name ="Skin Contact Detected",
                    value = skinContactDetected
                )
            )
        )
    }

    private fun has8BitHeartRate(flags: Int): Boolean {
        return flags and 0x01 == 0
    }

    private fun hasEnergyExpended(flags: Int): Boolean {
        return flags and 0x08 != 0
    }

    private fun hasRRInterval(flags: Int): Boolean {
        return flags and 0x10 != 0
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}