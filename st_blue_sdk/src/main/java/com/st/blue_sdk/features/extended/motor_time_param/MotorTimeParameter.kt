/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.motor_time_param

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class MotorTimeParameter(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MotorTimeParameterInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "MotorTimeParameter"
        const val NUMBER_BYTES = 18
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MotorTimeParameterInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            readByte = NUMBER_BYTES,
            rawData = data,
            data = MotorTimeParameterInfo(
                accPeakX = FeatureField(
                    name = "Acc X Peak",
                    unit = "m/s^2",
                    min = -2000f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToInt16(
                        data,
                        dataOffset + 0
                    ) / 100.0f
                ),
                accPeakY = FeatureField(
                    name = "Acc Y Peak",
                    unit = "m/s^2",
                    min = -2000f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToInt16(
                        data,
                        dataOffset + 2
                    ) / 100.0f
                ),
                accPeakZ = FeatureField(
                    name = "Acc Z Peak",
                    unit = "m/s^2",
                    min = -2000f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToInt16(
                        data,
                        dataOffset + 4
                    ) / 100.0f
                ),
                rmsSpeedX = FeatureField(
                    name = "RMS Speed X",
                    unit = "mm/s",
                    min = 0f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 6)
                ),
                rmsSpeedY = FeatureField(
                    name = "RMS Speed Y",
                    unit = "mm/s",
                    min = 0f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 10)
                ),
                rmsSpeedZ = FeatureField(
                    name = "RMS Speed Z",
                    unit = "mm/s",
                    min = 0f,
                    max = 2000f,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 14)
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}