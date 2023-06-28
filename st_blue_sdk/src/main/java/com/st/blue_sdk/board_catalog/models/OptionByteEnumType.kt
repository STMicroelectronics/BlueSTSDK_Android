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
data class OptionByteEnumType(
    @SerialName(value = "type")
    val type: String? = null,
    @SerialName(value = "display_name")
    val displayName: String? = null,
    @SerialName(value = "comment")
    val comment: String? = null,
    @SerialName(value = "value")
    val value: Int? = null,
    @SerialName(value = "icon_code")
    val iconCode: Int? = null
)
