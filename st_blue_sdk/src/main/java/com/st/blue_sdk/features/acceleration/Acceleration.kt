/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.acceleration

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Acceleration(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AccelerationInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val DATA_MAX = 16000f
        const val NUMBER_BYTES = 6
        const val NAME = "Acceleration"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AccelerationInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val acceleration = AccelerationInfo(
            x = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset).toFloat(),
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "mg",
                name = "X"
            ),
            y = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2).toFloat(),
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "mg",
                name = "Y"
            ),
            z = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4).toFloat(),
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "mg",
                name = "Z"
            )
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = acceleration
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}