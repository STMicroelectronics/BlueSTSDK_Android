/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.network_status

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class DeviceConnectionInfo(val deviceId: Byte, val isConnected: Boolean)

@Serializable
class NetworkStatusInfo(val connectionStatus: FeatureField<List<DeviceConnectionInfo>>) : Loggable {

    override val logHeader: String = connectionStatus.logHeader

    override val logValue: String = connectionStatus.logValue

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {

        val builder = StringBuilder()

        connectionStatus.value.forEach { connectionInfo ->
            builder.append("Device with ID ${connectionInfo.deviceId} is ${if (connectionInfo.isConnected) "CONNECTED" else "DISCONNECTED"}")
        }

        return builder.toString()
    }
}