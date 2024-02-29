/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.audio.adpcm

import com.st.blue_sdk.features.*

class AudioADPCMSyncFeature(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AudioADPCMSync>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false
) {
    companion object {
        const val NUMBER_BYTES = 6
        const val NAME = "AudioADPCMSyncFeature"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AudioADPCMSync> {

        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        var index: Int = ((data[0].toInt()) and 0x00FF)
        index = index or ((data[1].toInt()) shl 8 and 0xFF00)

        var preSample: Int = ((data[2].toInt()) and 0x000000FF)
        preSample = preSample or (((data[3].toInt()) shl 8) and 0x0000FF00)
        preSample = preSample or (((data[4].toInt()) shl 16) and 0x00FF0000)
        preSample = preSample or (((data[5].toInt()) shl 24) and 0xFF000000.toInt())

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            timeStamp = timeStamp,
            data = AudioADPCMSync(
                index = FeatureField(
                    name = "index",
                    value = index.toShort()
                ),
                predSample = FeatureField(
                    name = "preSample",
                    value = preSample
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}