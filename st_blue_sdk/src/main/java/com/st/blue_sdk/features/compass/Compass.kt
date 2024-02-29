/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.compass

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Compass(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<CompassInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Compass"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<CompassInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val compass = FeatureField(
            value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset) / 100.0f,
            max = 360f,
            min = 0f,
            unit = "°",
            name = "Angle"
        )

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = CompassInfo(compass),
            timeStamp = timeStamp
        )
    }


    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is StartCalibration -> {
                packCommandRequest(
                    featureBit,
                    FEATURE_COMMAND_START_CONFIGURATION,
                    byteArrayOf()
                )
            }
            is StopCalibration -> {
                packCommandRequest(
                    featureBit,
                    FEATURE_COMMAND_STOP_CONFIGURATION,
                    byteArrayOf()
                )
            }
            is GetCalibration -> {
                packCommandRequest(
                    featureBit,
                    FEATURE_COMMAND_GET_CONFIGURATION_STATUS,
                    byteArrayOf()
                )
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return unpackCommandResponse(data)?.let {
            val status = it.payload[0].toInt()

            when (val commandId = it.commandId) {
                FEATURE_COMMAND_GET_CONFIGURATION_STATUS -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = commandId,
                        status = status == 100
                    )
                }
                FEATURE_COMMAND_START_CONFIGURATION -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = commandId,
                        status = status == 100
                    )
                }
                FEATURE_COMMAND_STOP_CONFIGURATION -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = commandId,
                        status = status == 100
                    )
                }
                else -> null
            }
        }
    }
}