/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.co_sensor

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class COSensor(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<COSensorInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NUMBER_BYTES = 4
        const val NAME = "CO Sensor"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<COSensorInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val coSensor = FeatureField(
            value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset) / 100.0f,
            max = 1000000f,
            min = 0f,
            unit = "ppm",
            name = "CO Concentration"
        )

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = COSensorInfo(coSensor),
            timeStamp = timeStamp
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}