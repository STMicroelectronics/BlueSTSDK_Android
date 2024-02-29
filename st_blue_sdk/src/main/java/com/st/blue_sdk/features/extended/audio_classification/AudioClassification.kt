/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio_classification

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo.Companion.ALGORITHM_NOT_DEFINED
import com.st.blue_sdk.utils.NumberConversion

class AudioClassification(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AudioClassificationInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Audio Classification"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AudioClassificationInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val numberOfBytes = minOf(data.size - dataOffset, 2)

        val audioClassification =
            if (numberOfBytes == 1) {
                AudioClassificationInfo(
                    classification = FeatureField(
                        value = getAudioClassification(data[dataOffset].toShort()),
                        name = "AudioClassification"
                    ),
                    algorithm = FeatureField(
                        value = ALGORITHM_NOT_DEFINED,
                        name = "AudioClassification"
                    )
                )
            } else {
                AudioClassificationInfo(
                    classification = FeatureField(
                        value = getAudioClassification(data[dataOffset].toShort()),
                        name = "AudioClassification"
                    ),
                    algorithm = FeatureField(
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "AudioClassification"
                    )
                )
            }

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = numberOfBytes,
            data = audioClassification
        )

    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}