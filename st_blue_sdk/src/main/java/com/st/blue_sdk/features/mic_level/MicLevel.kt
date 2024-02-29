/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.mic_level

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class MicLevel(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MicLevelInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Microphone Level"
        const val DATA_MAX: Short = 128
        const val DATA_MIN: Short = 0
    }

    var numMic = 1

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MicLevelInfo> {
        val numberOfBytes = data.size - dataOffset
        require(numberOfBytes > 0) { "There are no bytes available to read for $name feature" }

        val levels = mutableListOf<FeatureField<Short>>()

        //In order to plot a variable number of Microphone levels
        numMic = numberOfBytes

        for (micNum in 0 until numberOfBytes) {
            levels.add(
                FeatureField(
                    max = DATA_MAX,
                    min = DATA_MIN,
                    name = "Mic_$micNum",
                    unit = "dB",
                    value = NumberConversion.byteToUInt8(data, dataOffset + micNum)
                )
            )
        }

        val micLevelInfo = MicLevelInfo(micsLevel = levels)

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = numberOfBytes,
            data = micLevelInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}
