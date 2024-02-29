/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.sensor_fusion

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class MemsSensorFusion(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MemsSensorFusionInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {

        const val NAME = "MemsSensorFusion"
        const val NUMBER_BYTES = 12
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MemsSensorFusionInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        var numBytes = NUMBER_BYTES
        val qi = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 0)
        val qj = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 4)
        val qk = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 8)
        val qs: Float =
            if ((data.size - dataOffset) > 12) {
                numBytes += 4
                NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 12);
            } else {
                Quaternion.getQs(qi, qj, qk)
            }

        val quaternion =
            FeatureField(value = Quaternion(timeStamp, qi, qj, qk, qs), name = "quaternion")

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = numBytes,
            data = MemsSensorFusionInfo(quaternions = listOf(quaternion))
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
            val status = it.payload[0].toInt()

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