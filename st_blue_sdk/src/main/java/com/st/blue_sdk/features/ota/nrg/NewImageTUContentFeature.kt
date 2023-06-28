/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.ota.nrg.request.ImageTUUpload
import com.st.blue_sdk.features.ota.nrg.response.ImageTUContentInfo

class NewImageTUContentFeature(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_BLUE_NRG_OTA,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<ImageTUContentInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp
) {

    companion object {
        const val NAME = "NewImageTUContentFeature"
        const val UPLOAD_COMMAND: Byte = 0x01
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ImageTUContentInfo> {

        val numByte = 1
        require(data.size - dataOffset >= numByte) { "There are byte available to read" }
        return FeatureUpdate(
            readByte = numByte,
            timeStamp = timeStamp,
            data = ImageTUContentInfo(
                FeatureField(
                    name = "ExpectedWriteLength",
                    value = data.size,
                    min = 0,
                    max = 0xFF
                )
            ),
            rawData = data
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is ImageTUUpload -> command.payload
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}