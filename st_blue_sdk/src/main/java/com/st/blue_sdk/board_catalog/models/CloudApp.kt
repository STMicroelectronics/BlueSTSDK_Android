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
data class CloudApp(
    @SerialName(value = "dtmi")
    val dtmi: String? = null,
    @SerialName(value = "name")
    var name: String? = null,
    @SerialName(value = "shareable_link")
    var shareableLink: String? = null,
    @SerialName(value = "url")
    var url: String? = null,
    @SerialName(value = "dtmi_type")
    var dtmiType: String? = null,
    @SerialName(value = "description")
    val description: String? = null
)
