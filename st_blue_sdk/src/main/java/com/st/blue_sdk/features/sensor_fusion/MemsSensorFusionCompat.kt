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

class MemsSensorFusionCompat(
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

        const val NAME = "MemsSensorFusionCompat"

        private const val SCALE_FACTOR = 10000.0f

        const val NUMBER_BYTES = 6
        const val QUATERNION_DELAY_MS = 30
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MemsSensorFusionInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val nQuat = (data.size - dataOffset) / NUMBER_BYTES
        val quaternionDelay = QUATERNION_DELAY_MS / nQuat

        val quaternions = mutableListOf<FeatureField<Quaternion>>()
        for (i in 0 until nQuat) {
            val ts = timeStamp + (i * quaternionDelay)
            val qi: Float =
                NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 0) / SCALE_FACTOR
            val qj: Float =
                NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2) / SCALE_FACTOR
            val qk: Float =
                NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4) / SCALE_FACTOR

            val qs = Quaternion.getQs(qi, qj, qk)
            quaternions.add(
                FeatureField(name = "quaternion", value = Quaternion(ts, qi, qj, qk, qs))
            )
        }

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = nQuat * NUMBER_BYTES,
            data = MemsSensorFusionInfo(quaternions = quaternions)
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