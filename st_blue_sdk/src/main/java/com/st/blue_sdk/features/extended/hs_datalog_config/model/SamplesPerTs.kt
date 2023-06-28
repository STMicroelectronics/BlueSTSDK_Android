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
data class SamplesPerTs(
    @SerialName("min") val min: Int,
    @SerialName("max") val max: Int,
    @SerialName("dataType") val dataType: String
)