/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.gyroscope

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Gyroscope(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<GyroscopeInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Gyroscope"
        const val DATA_MAX = ((1 shl 15)).toFloat() / 10.0f
        const val NUMBER_BYTES = 6
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<GyroscopeInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val gyroscope = GyroscopeInfo(
            x = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset).toFloat()/10.0f,
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "dps",
                name = "X"
            ),
            y = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2).toFloat()/10.0f,
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "dps",
                name = "Y"
            ),
            z = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4).toFloat()/10.0f,
                max = DATA_MAX,
                min = -DATA_MAX,
                unit = "dps",
                name = "Z"
            )
        )

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = gyroscope
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}