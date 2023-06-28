/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.predictive

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class PredictiveFrequencyStatus(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PredictiveFrequencyStatusInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "PredictiveFrequencyStatus"
        const val NUMBER_BYTES = 13
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<PredictiveFrequencyStatusInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val timeStatus = NumberConversion.byteToUInt8(data, dataOffset + 0)
        return FeatureUpdate(
            timeStamp = timeStamp,
            readByte = NUMBER_BYTES,
            rawData = data,
            data = PredictiveFrequencyStatusInfo(
                statusX = FeatureField(
                    name = "StatusAcc_X",
                    unit = "m/s^2",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractXStatus(timeStatus)
                ),
                statusY = FeatureField(
                    name = "StatusAcc_Y",
                    unit = "m/s^2",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractYStatus(timeStatus)
                ),
                statusZ = FeatureField(
                    name = "StatusAcc_Z",
                    unit = "m/s^2",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractZStatus(timeStatus)
                ),
                worstXFreq = FeatureField(
                    name = "Freq_X",
                    unit = "Hz",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 1
                    ) / 10.0f
                ),
                worstYFreq = FeatureField(
                    name = "Freq_Y",
                    unit = "Hz",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 5
                    ) / 10.0f
                ),
                worstZFreq = FeatureField(
                    name = "Freq_Z",
                    unit = "Hz",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 9
                    ) / 10.0f
                ),
                worstXValue = FeatureField(
                    name = "MaxAmplitude_X",
                    unit = "m/s^2",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 3
                    ) / 100.0f
                ),
                worstYValue = FeatureField(
                    name = "MaxAmplitude_Y",
                    unit = "m/s^2",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 7
                    ) / 100.0f
                ),
                worstZValue = FeatureField(
                    name = "MaxAmplitude_Z",
                    unit = "m/s^2",
                    min = 0f,
                    max = (1 shl 16) / 10.0f,
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 11
                    ) / 100.0f
                ),
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}