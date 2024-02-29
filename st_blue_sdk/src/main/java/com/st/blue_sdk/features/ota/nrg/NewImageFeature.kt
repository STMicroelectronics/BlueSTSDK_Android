/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.ota.nrg.request.WriteNewImageParameter
import com.st.blue_sdk.features.ota.nrg.response.NewImageInfo
import com.st.blue_sdk.utils.NumberConversion

class NewImageFeature(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_BLUE_NRG_OTA,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<NewImageInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp
) {

    companion object {
        const val NAME = "NewImage"

        const val SEND_PARAMETER: Byte = 0x01
    }

    fun setMaxPayLoadSize(payLoadSize: Int) {
        maxPayloadSize = payLoadSize
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<NewImageInfo> {

        val numByte = 9

        require(data.size - dataOffset >= numByte) { "There are byte available to read" }

        val otaAckEvery = FeatureField(
            name = "otaAckEvery",
            value = data[dataOffset],
            max = 0xFF.toByte(),
            min = 0,
        )

        val imageSize = FeatureField(
            name = "imageSize",
            value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 1),
            max = 0xFFFFFFFF,
            min = 0
        )
        val baseAddress = FeatureField(
            name = "baseAddress",
            value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 5),
            max = 0xFFFFFFFF,
            min = 0
        )

        return FeatureUpdate(
            featureName = name,
            readByte = 9,
            timeStamp = timeStamp,
            data = NewImageInfo(otaAckEvery, imageSize, baseAddress),
            rawData = data
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is WriteNewImageParameter -> {
                val imageSize = NumberConversion.LittleEndian.uint32ToBytes(command.imageSize)
                val imageAddress = NumberConversion.LittleEndian.uint32ToBytes(command.baseAddress)
                return byteArrayOf(command.otaAckEvery) + imageSize + imageAddress
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}