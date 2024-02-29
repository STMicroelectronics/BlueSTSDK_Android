/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.qvar

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class QVAR(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<QVARInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Electric Charge Variation"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<QVARInfo> {
        require(data.size - dataOffset >= 4) { "There are no 4 bytes available to read for $name feature" }

        var numBytes = 4
        val qvar: FeatureField<Long> = FeatureField(
            value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset).toLong(),
            max = Long.MAX_VALUE,
            min = Long.MIN_VALUE,
            unit = "LSB",
            name = "QVAR"
        )

        val flag: FeatureField<Byte?> =
            FeatureField(
                value =
                if (data.size - dataOffset >= 5) {
                    numBytes += 1

                    data[dataOffset + 4]
                } else
                    null,
                name = "Flag"
            )

        val dqvar: FeatureField<Long?> =
            FeatureField(
                value = if (data.size - dataOffset >= 9) {
                    numBytes += 4

                    NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 5).toLong()
                } else
                    null,
                max = Long.MAX_VALUE,
                min = Long.MIN_VALUE,
                unit = "LSB",
                name = "DQVAR"
            )
        val param: FeatureField<Long?> =
            FeatureField(
                value =
                if (data.size - dataOffset == 13) {
                    numBytes += 4

                    NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 9)
                        .toLong()
                } else
                    null,
                name = "Parameter"
            )

        //Filling the QVAR info
        val qvarInfo = QVARInfo(
            qvar = qvar,
            flag = flag,
            dqvar = dqvar,
            param = param
        )

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = numBytes, data = qvarInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}