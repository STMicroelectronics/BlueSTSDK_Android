/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.motion_intensity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class MotionIntensity(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MotionIntensityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Vertical Context Intensity"
        const val NUMBER_BYTES = 1
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MotionIntensityInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val intensity = FeatureField(
            value = NumberConversion.byteToUInt8(data, dataOffset),
            max = 10,
            min = 0,
            name = "Intensity"
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = MotionIntensityInfo(intensity)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}