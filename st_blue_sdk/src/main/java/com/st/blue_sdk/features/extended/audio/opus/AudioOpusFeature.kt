/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio.opus

import com.st.blue_sdk.features.*

class AudioOpusFeature(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<RawAudio>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "AudioOpusFeature"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RawAudio> {

        return FeatureUpdate(
            rawData = data,
            readByte = data.size,
            timeStamp = timeStamp,
            data = RawAudio(
                FeatureField(
                    name = "data",
                    value = data
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}