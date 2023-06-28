/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.ota.nrg.response.ExpectedImageSeqNumber
import com.st.blue_sdk.utils.NumberConversion

class ExpectedImageTUSeqNumberFeature(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_BLUE_NRG_OTA,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<ExpectedImageSeqNumber>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp
) {

    companion object {
        const val NAME = "ExpectedImageTUSeqNumberFeature"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ExpectedImageSeqNumber> {

        val numByte = 3
        require(data.size - dataOffset >= numByte) { "There are byte available to read" }

        val nextExpectedCharBlock = FeatureField(
            name = "nextExpectedCharBlock",
            max = 0xFFFF,
            min = 0,
            value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)
        )

        val errorAck = FeatureField(
            name = "errorAck",
            value = ExpectedImageSeqNumber.ErrorCode.buildErrorCode(data[dataOffset + 2])
        )

        return FeatureUpdate(
            readByte = numByte,
            timeStamp = timeStamp,
            data = ExpectedImageSeqNumber(nextExpectedCharBlock, errorAck),
            rawData = data
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}