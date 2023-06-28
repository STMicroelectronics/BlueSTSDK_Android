/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.humidity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Humidity(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<HumidityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Humidity"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<HumidityInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val humidity = FeatureField(
            value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset) / 10.0f,
            max = 100f,
            min = 0f,
            unit = "%",
            name = "Humidity"
        )

        return FeatureUpdate(
            rawData = data,
            readByte = NUMBER_BYTES,
            data = HumidityInfo(humidity),
            timeStamp = timeStamp
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}