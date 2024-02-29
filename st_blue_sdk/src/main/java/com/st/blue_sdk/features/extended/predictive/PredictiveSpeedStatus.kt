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

class PredictiveSpeedStatus(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PredictiveSpeedStatusInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = true
) {
    companion object {
        const val NAME = "PredictiveSpeedStatus"
        const val NUMBER_BYTES = 12
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<PredictiveSpeedStatusInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val timeStatus = NumberConversion.byteToUInt8(data, dataOffset + 0)

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            readByte = NUMBER_BYTES,
            rawData = data,
            data = PredictiveSpeedStatusInfo(
                statusX = FeatureField(
                    name = "StatusSpeed_X",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractXStatus(timeStatus)
                ),
                statusY = FeatureField(
                    name = "StatusSpeed_Y",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractYStatus(timeStatus)
                ),
                statusZ = FeatureField(
                    name = "StatusSpeed_Z",
                    min = Status.GOOD,
                    max = Status.BAD,
                    value = Status.extractZStatus(timeStatus)
                ),
                speedX = FeatureField(
                    name = "RMSSpeed_X",
                    unit = "mm/s",
                    min = 0f,
                    max = Float.MAX_VALUE,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 1)
                ),
                speedY = FeatureField(
                    name = "RMSSpeed_Y",
                    unit = "mm/s",
                    min = 0f,
                    max = Float.MAX_VALUE,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 5)
                ),
                speedZ = FeatureField(
                    name = "RMSSpeed_Z",
                    unit = "mm/s",
                    min = 0f,
                    max = Float.MAX_VALUE,
                    value = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 9)
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}