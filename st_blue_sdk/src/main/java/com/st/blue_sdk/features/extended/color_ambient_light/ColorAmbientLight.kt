/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.color_ambient_light

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class ColorAmbientLight(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<ColorAmbientLightInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Color Ambient Light"
        const val NUMBER_BYTES = 8
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ColorAmbientLightInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val colorAmbientLight = ColorAmbientLightInfo(
            lux = FeatureField(
                value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset).toInt(),
                max = 400000,
                min = 0,
                unit = "Lux",
                name = "Lux"
            ),
            cct = FeatureField(
                value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 4).toShort(),
                max = 20000,
                min = 0,
                unit = "K",
                name = "Correlated Color Temperature"
            ),
            uvIndex = FeatureField(
                value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 6).toShort(),
                max = 12,
                min = 0,
                name = "UV Index"
            )
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = colorAmbientLight
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}