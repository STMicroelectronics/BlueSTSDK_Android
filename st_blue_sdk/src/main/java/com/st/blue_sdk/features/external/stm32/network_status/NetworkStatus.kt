/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.network_status

import com.st.blue_sdk.features.*

class NetworkStatus(
    isEnabled: Boolean,
    type: Type,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = true
) : Feature<NetworkStatusInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = false
) {

    companion object {
        private const val NAME = "Network Status"

        private const val MAX_MANAGED_DEVICE: Byte = 6
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<NetworkStatusInfo> {

        require(data.size - dataOffset >= MAX_MANAGED_DEVICE) { "There are no $MAX_MANAGED_DEVICE bytes available to read" }

        val connectionInfos = data.mapIndexed { index, connectionFlag ->
            DeviceConnectionInfo(
                index.toByte(),
                connectionFlag == 0x01.toByte()
            )
        }

        return FeatureUpdate(
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = NetworkStatusInfo(FeatureField(name = "NetworkInfo", value = connectionInfos))
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}