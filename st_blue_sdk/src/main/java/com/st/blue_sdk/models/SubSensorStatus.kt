/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubSensorStatus(
    val isActive: Boolean,
    @SerialName("ODR") val odr: Double?,
    @SerialName("ODRMeasured") val odrMeasured: Double?,
    val initialOffset: Double?,
    val samplesPerTs: Int,
    @SerialName("FS") val fs: Double?,
    val sensitivity: Double?,
    val usbDataPacketSize: Int,
    val sdWriteBufferSize: Int,
    val wifiDataPacketSize: Int,
    val comChannelNumber: Int,
    val ucfLoaded: Boolean,
    //var paramsLocked: Boolean = false
)