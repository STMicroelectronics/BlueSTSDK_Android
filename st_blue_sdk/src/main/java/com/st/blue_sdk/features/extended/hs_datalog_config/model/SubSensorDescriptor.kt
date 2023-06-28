/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.hs_datalog_config.model

import com.st.blue_sdk.board_catalog.models.SensorType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubSensorDescriptor(
    @SerialName("id") val id: Int,
    @SerialName("sensorType") val sensorType: SensorType,
    @SerialName("dimensions") val dimensions: Int,
    @SerialName("dimensionsLabel") val dimensionsLabel: List<String>,
    @SerialName("unit") val unit: String?,
    @SerialName("dataType") val dataType: String?,
    @SerialName("FS") val fs: List<Double>?,
    @SerialName("ODR") val odr: List<Double>?,
    @SerialName("samplesPerTs") val samplesPerTs: SamplesPerTs
)
