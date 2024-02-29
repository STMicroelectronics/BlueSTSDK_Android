/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.event_counter

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class EventCounter(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<EventCounterInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Event Counter"
        const val NUMBER_BYTES = 4
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<EventCounterInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }
        val eventCounter = FeatureField(
            value = NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset).toInt(),
            name = "Event"
        )

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = EventCounterInfo(eventCounter),
            timeStamp = timeStamp
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}