/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.temperature

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Temperature(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<TemperatureInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Temperature"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<TemperatureInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }
        val temperature = FeatureField(
            value = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset) / 10.0f,
            max = 120f,
            min = -40f,
            unit = "\u2103", // celsius degree
            name = "Temperature"
        )
        val featureId = FeatureField(
            value = this.hashCode(),
            name = "Feature Id"
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = TemperatureInfo(temperature, featureId)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}