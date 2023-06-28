/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BleCharacteristicFormat(
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "optional")
    val optional: Boolean = false,
    @SerialName(value = "length")
    val length: Int?=null,
    @SerialName(value = "unit")
    val unit: String? = null,
    @SerialName(value = "type")
    val type: Field.Type? = null,
    @SerialName(value = "offset")
    val offset: Float?=null,
    @SerialName(value = "scalefactor")
    val scalefactor: Float?=null,
    @SerialName(value = "min")
    val min: Float? = null,
    @SerialName(value = "max")
    val max: Float? = null,
    @SerialName(value = "string_values")
    val stringValues: List<BleCharStringValue>? = null
)
