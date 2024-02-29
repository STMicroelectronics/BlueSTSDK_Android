/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.euler_angle

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class EulerAngle(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<EulerAngleInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Auler Angle"
        const val NUMBER_BYTES = 12
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<EulerAngleInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val eulerAngle = EulerAngleInfo(
            yaw = FeatureField(
                value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset),
                max = 360f,
                min = 0f,
                unit = "°",
                name = "Yaw"
            ),
            pitch = FeatureField(
                value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 4),
                max = 180f,
                min = -180f,
                unit = "°",
                name = "Pitch"
            ),
            roll = FeatureField(
                value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 8),
                max = 90f,
                min = -90f,
                unit = "°",
                name = "Roll"
            )
        )

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = eulerAngle
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is StartCalibration -> packCommandRequest(
                featureBit,
                FEATURE_COMMAND_START_CONFIGURATION,
                byteArrayOf()
            )
            is StopCalibration -> packCommandRequest(
                featureBit,
                FEATURE_COMMAND_STOP_CONFIGURATION,
                byteArrayOf()
            )
            is GetCalibration -> packCommandRequest(
                featureBit,
                FEATURE_COMMAND_GET_CONFIGURATION_STATUS,
                byteArrayOf()
            )
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {

        return unpackCommandResponse(data)?.let {
            val status =  it.payload[0].toInt()

            when (it.commandId) {
                FEATURE_COMMAND_GET_CONFIGURATION_STATUS -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = it.commandId,
                        status = status == 100
                    )
                }
                FEATURE_COMMAND_START_CONFIGURATION -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = it.commandId,
                        status = status == 100
                    )
                }
                FEATURE_COMMAND_STOP_CONFIGURATION -> {
                    CalibrationStatus(
                        feature = this,
                        commandId = it.commandId,
                        status = status == 100
                    )
                }
                else -> null
            }
        }
    }
}