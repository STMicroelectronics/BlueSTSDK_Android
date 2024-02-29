/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.proximity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.proximity.ProximityInfo.Companion.HIGH_RANGE_DATA_MAX
import com.st.blue_sdk.features.proximity.ProximityInfo.Companion.LOW_RANGE_DATA_MAX
import com.st.blue_sdk.features.proximity.ProximityInfo.Companion.OUT_OF_RANGE_VALUE
import com.st.blue_sdk.utils.NumberConversion

class Proximity(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<ProximityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Proximity"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ProximityInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val proximity = FeatureField(
            value = getRangeValue(NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)),
            max = OUT_OF_RANGE_VALUE,
            min = 0,
            unit = "mm",
            name = "Distance"
        )

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = ProximityInfo(proximity = proximity)
        )
    }

    private fun getRangeValue(range: Int): Int {
        val rangeValidValue = range and (0x8000.inv())
        val outRange =
            if ((range and 0x8000) == 0) {
                //low Range
                if (rangeValidValue > LOW_RANGE_DATA_MAX)
                    OUT_OF_RANGE_VALUE
                else
                    rangeValidValue
            } else {
                //High Range
                if (rangeValidValue > HIGH_RANGE_DATA_MAX)
                    OUT_OF_RANGE_VALUE
                else
                    rangeValidValue
            }
        return outRange
    }


    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}