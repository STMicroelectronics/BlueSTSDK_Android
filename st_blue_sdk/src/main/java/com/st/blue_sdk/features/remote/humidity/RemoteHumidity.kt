/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.humidity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import com.st.blue_sdk.utils.UnwrapTimestamp

/**
 * Class that manage an humidity that is acquired by a node and send by another node
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
class RemoteHumidity(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME
) :
    Feature<RemoteHumidityInfo>(
        isEnabled, type,
        identifier, name
    ) {

    companion object {
        const val NAME = "Remote Humidity"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RemoteHumidityInfo> {

        require(data.size - dataOffset >= 3) { "There are enough bytes available to read" }

        //remove multiple of 2^16 since the node can unwrap the timestamp
        val remoteId = timeStamp.toInt() % (1 shl 16)

        val ts = NumberConversion.BigEndian.bytesToUInt16(data, dataOffset).toLong()
        val remoteTs = UnwrapTimestamp().unwrap(ts)
        val humidity = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 2) / 10.0f

        return FeatureUpdate(
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = RemoteHumidityInfo(
                remoteNodeId = FeatureField(name = "remoteNodeId", value = remoteId),
                remoteTimestamp = FeatureField(name = "remoteTimestamp", value = remoteTs),
                humidity = FeatureField(
                    name = "remoteHumidity",
                    max = 100f,
                    min = 0f,
                    value = humidity,
                    unit = "%"
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}