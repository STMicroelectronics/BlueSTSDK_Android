/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.temperature

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import com.st.blue_sdk.utils.UnwrapTimestamp

class RemoteTemperature(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = true
) : Feature<RemoteTemperatureInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = true
) {

    companion object {
        const val NAME = "REMOTE TEMPERATURE"
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RemoteTemperatureInfo> {

        require(data.size - dataOffset >= 3) { "There are enough bytes available to read" }

        //remove multiple of 2^16 since the node can unwrap the timestamp
        val remoteId = timeStamp.toInt() % (1 shl 16)

        val ts = NumberConversion.BigEndian.bytesToUInt16(data, dataOffset).toLong()
        val remoteTs = UnwrapTimestamp().unwrap(ts)
        val temperature = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2) / 10.0f

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = RemoteTemperatureInfo(
                remoteNodeId = FeatureField(name = "remoteNodeId", value = remoteId),
                remoteTimestamp = FeatureField(name = "remoteTimestamp", value = remoteTs),
                temperature = FeatureField(
                    name = "remoteTemperature",
                    max = 120f,
                    min = -40f,
                    value = temperature,
                    unit = "\u2103"
                )
            )
        )
    }
}