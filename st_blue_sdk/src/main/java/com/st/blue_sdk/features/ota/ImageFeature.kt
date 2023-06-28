/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class ImageFeature(
    name: String = NAME,
    type: Type = Type.EXTERNAL_BLUE_NRG_OTA,
    isEnabled: Boolean,
    identifier: Int,
) : Feature<ImageInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "ImageFeature"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ImageInfo> {
        var readData = 0
        val availableData = data.size - dataOffset

        require(availableData >= 8) { "There are no 8 bytes available to read" }

        val flashLB: Long = NumberConversion.BigEndian.bytesToUInt32(data, dataOffset)
        val flashUB: Long = NumberConversion.BigEndian.bytesToUInt32(data, dataOffset + 4)

        var versionMajor = 1
        var versionMinor = 0
        if (availableData >= 9) {
            versionMajor = data[dataOffset + 8] / 16
            versionMinor = data[dataOffset + 8] % 16
            readData++
        }
        val imageInfo = ImageInfo(
            FeatureField(
                value = flashLB,
                max = 0xFFFFFFFF,
                min = 0,
                unit = null,
                name = "Flash_LB"
            ),
            FeatureField(
                value = flashUB,
                max = 0xFFFFFFFF,
                min = 0,
                unit = null,
                name = "Flash_UB"
            ),
            FeatureField(
                value = versionMajor,
                max = 255,
                min = 0,
                unit = null,
                name = "ProtocolVerMajor"
            ),
            FeatureField(
                value = versionMinor,
                max = 255,
                min = 0,
                unit = null,
                name = "ProtocolVerMinor"
            )
        )

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = readData, data = imageInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}
