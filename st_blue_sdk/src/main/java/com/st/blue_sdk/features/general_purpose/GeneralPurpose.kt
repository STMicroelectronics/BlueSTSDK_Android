/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.general_purpose

import com.st.blue_sdk.features.*

class GeneralPurpose(
    name: String,
    type: Type = Type.GENERAL_PURPOSE,
    isEnabled: Boolean,
    identifier: Int
) : Feature<GeneralPurposeInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
) {
    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<GeneralPurposeInfo> {

        val rawValues = mutableListOf<FeatureField<Byte>>()

        for (dataNum in 0 until (data.size - dataOffset)) {
            rawValues.add(
                FeatureField(
                    name = "B$dataNum",
                    value = data[dataOffset + dataNum]
                )
            )
        }
        val generalPurpose = GeneralPurposeInfo(
            data = rawValues
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = data.size - dataOffset,
            data = generalPurpose
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}