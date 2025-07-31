/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.raw_controlled.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawPnPLEntryFormat(
    @SerialName(value = "enable")
    val enable: Boolean,
    @SerialName(value = "format")
    val format: RawPnPLEntryFormat,
    @SerialName(value = "elements")
    var elements: Int=1,
    @SerialName(value = "channels")
    var channels: Int=1,
    @SerialName(value= "multiply_factor")
    var multiplyFactor: Float?=null,
    @SerialName(value="odr")
    var odr: Int?=null,
    @SerialName(value = "unit")
    val unit: String?=null,
    @SerialName(value = "max")
    val max: Double?=null,
    @SerialName(value = "min")
    val min: Double?=null,
    @SerialName(value= "labels")
    val labels: String?=null
) {
    @kotlinx.serialization.Transient
    var values: MutableList<Any> = mutableListOf()

    @kotlinx.serialization.Transient
    var valuesFloat: MutableList<Float> = mutableListOf()

    @kotlinx.serialization.Transient
    var labelsParsed: List<RawPnPLEntryEnumLabel>?=null

    enum class RawPnPLEntryFormat {
        uint8_t,
        int8_t,
        uint16_t,
        int16_t,
        uint32_t,
        int32_t,
        float,
        float_t,
        enum,
        enum_t
    }
}



