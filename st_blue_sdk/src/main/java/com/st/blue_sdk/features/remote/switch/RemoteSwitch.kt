/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.switch

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import com.st.blue_sdk.utils.UnwrapTimestamp

/**
 * Class that manage a switch status that is acquired by a node and send by another node
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
class RemoteSwitch(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = true,
    isDataNotifyFeature: Boolean = true
) : Feature<RemoteFeatureSwitchInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = isDataNotifyFeature
) {

    companion object {
        const val NAME = "Remote switch"
        const val SET_REMOTE_SWITCH_COMMAND: Byte = 0x00
        const val COMMAND_SWITCH_OFF: Byte = 0x00
        const val COMMAND_SWITCH_ON: Byte = 0x01
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
    ): FeatureUpdate<RemoteFeatureSwitchInfo> {

        require(data.size - dataOffset >= 3) { "There are enough bytes available to read" }

        //remove multiple of 2^16 since the node can unwrap the timestamp
        val remoteId = timeStamp.toInt() % (1 shl 16)

        val ts = NumberConversion.BigEndian.bytesToUInt16(data, dataOffset).toLong()
        val remoteTs = UnwrapTimestamp().unwrap(ts)
        val switchStatus = data[dataOffset + 2]

        return FeatureUpdate(
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = RemoteFeatureSwitchInfo(
                remoteNodeId = FeatureField(name = "remoteNodeId", value = remoteId),
                remoteTimestamp = FeatureField(name = "remoteTimestamp", value = remoteTs),
                switchStatus = FeatureField(
                    "remoteSwitchStatus",
                    min = 0,
                    max = 255.toByte(),
                    value = switchStatus
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is ChangeRemoteSwitchStatusCommand -> {
                packCommandRequest(
                    featureBit,
                    command.newStatus,
                    NumberConversion.BigEndian.uint16ToBytes(command.nodeId)
                )
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}