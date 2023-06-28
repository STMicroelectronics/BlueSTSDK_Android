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
data class DeviceInfo(
    @SerialName("serialNumber") var serialNumber: String, // TODO: update ongoing FW side (probably this will be in a sort of UUID format (with no spaces))
    @SerialName("alias") var alias: String,
    @SerialName("partNumber") var partNumber: String? = null,
    @SerialName("URL") val URL: String? = null,
    @SerialName("fwName") val fwName: String? = null,
    @SerialName("fwVersion") val fwVersion: String? = null,
    @SerialName("dataFileExt") val dataFileExt: String? = null,
    @SerialName("dataFileFormat") val dataFileFormat: String? = null
)
