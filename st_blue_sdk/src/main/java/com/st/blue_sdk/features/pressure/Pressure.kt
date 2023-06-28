/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.pressure

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Pressure(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PressureInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Pressure"
        const val NUMBER_BYTES = 4
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<PressureInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val pressure = FeatureField(
            value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset) / 100.0f,
            max = 2000f,
            min = 0f,
            unit = "mBar",
            name = "Pressure"
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = PressureInfo(pressure)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return null
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}
