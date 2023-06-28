/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import com.st.blue_sdk.board_catalog.models.SensorType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubSensorDescriptor(
    val id: Int,
    val sensorType: SensorType,
    val dimensions: Int,
    val dimensionsLabel: List<String>,
    val unit: String?,
    val dataType: String?,
    @SerialName("FS") val fs: List<Double>?,
    @SerialName("ODR") val odr: List<Double>?,
    val samplesPerTs: SamplesPerTs
)

@Serializable
data class SamplesPerTs(
    val min: Int,
    val max: Int,
    val dataType: String
)