/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.direction_of_arrival

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.direction_of_arrival.request.SetSensitivityHigh
import com.st.blue_sdk.features.direction_of_arrival.request.SetSensitivityLow
import com.st.blue_sdk.utils.NumberConversion

class DirectionOfArrival(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<DirectionOfArrivalInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Direction of Arrival"
        const val NUMBER_BYTES = 2

        const val COMMAND_SET_SENSITIVITY: Byte = 0xCC.toByte()
        const val COMMAND_SENSITIVITY_LOW: Byte = 0x00.toByte()
        const val COMMAND_SENSITIVITY_HIGH: Byte = 0x01.toByte()
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<DirectionOfArrivalInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val direction = DirectionOfArrivalInfo(
            angle = FeatureField(
                value = normalizeAngle(
                    NumberConversion.LittleEndian.bytesToInt16(
                        data,
                        dataOffset
                    )
                ),
                name = "Angle",
                unit = "°"
            )
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = direction
        )
    }

    private fun normalizeAngle(angleIn: Short): Int {
        var angleOut = angleIn.toInt()
        while (angleOut < 0) {
            angleOut += 360
        }
        while (angleOut > 360) {
            angleOut -= 360
        }
        return angleOut
    }

    override fun packCommandData(
        featureBit: Int?,
        command: FeatureCommand
    ): ByteArray? {// TODO: To Be Checked
        return when (command) {
            is SetSensitivityHigh -> packCommandRequest(
                featureBit,
                COMMAND_SET_SENSITIVITY,
                byteArrayOf(0x01)

            )
            is SetSensitivityLow -> packCommandRequest(
                featureBit,
                COMMAND_SET_SENSITIVITY,
                byteArrayOf(0x00)
            )
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}