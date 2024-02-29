/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.pressure

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import com.st.blue_sdk.utils.UnwrapTimestamp

/**
 * Class that manage an pressure that is acquired by a node and send by another node
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
class RemotePressure(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = true
) : Feature<RemotePressureInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = true
) {

    companion object {
        const val NAME = "Remote Pressure"
    }

    /**
     * Extract the remote id and humidity from an array of bytes
     * @param timestamp for the remote feature is not the timestamp but the node id
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return remote Temperature sample and bytes read
     */
    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RemotePressureInfo> {

        require(data.size - dataOffset >= 3) { "There are enough bytes available to read" }

        //remove multiple of 2^16 since the node can unwrap the timestamp
        val remoteId = timeStamp.toInt() % (1 shl 16)

        val ts = NumberConversion.BigEndian.bytesToUInt16(data, dataOffset).toLong()
        val remoteTs = UnwrapTimestamp().unwrap(ts)
        val pressure = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 2) / 100.0f

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = RemotePressureInfo(
                remoteNodeId = FeatureField(name = "remoteNodeId", value = remoteId),
                remoteTimestamp = FeatureField(name = "remoteTimestamp", value = remoteTs),
                pressure = FeatureField(
                    name = "remotePressure",
                    max = 2000f,
                    min = 0f,
                    value = pressure,
                    unit = "mBar"
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}