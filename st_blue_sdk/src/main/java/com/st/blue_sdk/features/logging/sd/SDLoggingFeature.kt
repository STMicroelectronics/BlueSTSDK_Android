/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.logging.sd

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.logging.sd.commands.StartLogging
import com.st.blue_sdk.features.logging.sd.commands.StopLogging
import com.st.blue_sdk.utils.NumberConversion

class SDLoggingFeature(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME
) : Feature<SDLoggingInfo>(
    isEnabled = isEnabled, type = type,
    identifier = identifier,
    name = name,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "SDLogging"

        const val STOP_SD_LOGGING: Byte = 0x00
        const val START_SD_LOGGING: Byte = 0x01
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is StartLogging -> {
                val mask = buildFeatureMask(command.featureMasks)
                byteArrayOf(START_SD_LOGGING) +
                        NumberConversion.LittleEndian.uint32ToBytes(mask) +
                        NumberConversion.LittleEndian.uint32ToBytes(
                            command.interval
                        )
            }
            is StopLogging -> {
                byteArrayOf(
                    STOP_SD_LOGGING,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00
                )
            }
            else -> null
        }
    }

    private fun buildFeatureMask(featureIds: Set<Int>): Long {
        return featureIds.fold(0, operation = { acc, feature ->
            acc or feature.toLong()
        })
    }

    private fun buildFeatureSet(featureMask: Long): Set<Int> {
        val outCollection = HashSet<Int>(32)
        var mask = 1L shl 31
        for (i in 0..31) {
            val featureId = featureMask and mask
            if (featureId != 0L) {
                outCollection.add(featureId.toInt())
            }
            mask = mask shr 1
        }
        return outCollection
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<SDLoggingInfo> {

        require(data.size - dataOffset >= 9) { "There are no 9 bytes available to read" }

        val isEnabledRowValue = data[dataOffset]
        val logStatusValue = when (isEnabledRowValue.toInt()) {
            0x00 -> LoggingStatus.STOPPED
            0x01 -> LoggingStatus.STARTED
            0x02 -> LoggingStatus.NO_SD
            else -> LoggingStatus.IO_ERROR
        }

        val logStatus = FeatureField(name = "isEnabled", value = logStatusValue)

        val ids = buildFeatureSet(NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 1))
        val featureIds = FeatureField(
            name = "loggedFeature",
            value = ids,
        )

        val logInterval = FeatureField(
            name = "logInterval",
            value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 5),
            min = 0,
            max = 1L shl 32 - 1
        )

        return FeatureUpdate(
            readByte = 9,
            timeStamp = timeStamp,
            rawData = data,
            data = SDLoggingInfo(logStatus, featureIds, logInterval)
        )
    }
}