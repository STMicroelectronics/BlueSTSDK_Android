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
data class SubSensorStatus(
    @SerialName("isActive") var isActive: Boolean,
    @SerialName("ODR") var odr: Double?,
    @SerialName("ODRMeasured") val odrMeasured: Double?,
    @SerialName("initialOffset") val initialOffset: Double?,
    @SerialName("samplesPerTs") var samplesPerTs: Int,
    @SerialName("FS") var fs: Double?,
    @SerialName("sensitivity") val sensitivity: Double?,
    @SerialName("usbDataPacketSize") val usbDataPacketSize: Int,
    @SerialName("sdWriteBufferSize") val sdWriteBufferSize: Int,
    @SerialName("wifiDataPacketSize") val wifiDataPacketSize: Int,
    @SerialName("comChannelNumber") val comChannelNumber: Int,
    @SerialName("ucfLoaded") var ucfLoaded: Boolean
)
