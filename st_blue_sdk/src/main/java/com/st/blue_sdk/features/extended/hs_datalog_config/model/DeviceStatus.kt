/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.hs_datalog_config.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    @SerialName("type") val type: String? = null,
    @SerialName("isLogging") val isSDLogging: Boolean? = null,
    @SerialName("isSDInserted") val isSDCardInserted: Boolean? = null,
    @SerialName("cpuUsage") val cpuUsage: Double? = null,
    @SerialName("batteryVoltage") val batteryVoltage: Double? = null,
    @SerialName("batteryLevel") val batteryLevel: Double? = null,
    @SerialName("ssid") val ssid: String? = null,
    @SerialName("password") val password: String? = null,
    @SerialName("ip") val ip: String? = null,
    @SerialName("sensorId") val sensorId: Int? = null,
    @SerialName("sensorStatus") val sensorStatus: SensorStatus? = null
)
