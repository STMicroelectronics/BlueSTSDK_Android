/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.pedometer

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Pedometer(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PedometerInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Pedometer"
        const val NUMBER_BYTES = 6
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<PedometerInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val pedometer = PedometerInfo(
            steps = FeatureField(
                max = Long.MAX_VALUE,
                min = 0,
                name = "Steps",
                unit = "dB",
                value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset)
            ),
            frequency = FeatureField(
                max = Short.MAX_VALUE,
                min = 0,
                name = "Frequency",
                unit = "Steps/Min",
                value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 4).toShort()
            )
        )
        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = pedometer
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}