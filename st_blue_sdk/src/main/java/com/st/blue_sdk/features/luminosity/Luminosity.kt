/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.luminosity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class Luminosity(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<LuminosityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Luminosity"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LuminosityInfo> {
        require(data.size - dataOffset >= 2) { "There are no 2 bytes available to read for $name feature" }

        val luminosity = FeatureField(
            value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset),
            max = 1000,
            min = 0,
            unit = "Lux",
            name = "Luminosity"
        )

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = 2,
            data = LuminosityInfo(luminosity),
            timeStamp = timeStamp
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}